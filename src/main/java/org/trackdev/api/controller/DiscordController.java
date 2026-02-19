package org.trackdev.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.configuration.TrackDevProperties;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.entity.User;
import org.trackdev.api.service.DiscordService;
import org.trackdev.api.service.UserService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Base64;
import java.util.Map;

@Tag(name = "20. Discord Integration")
@RestController
@RequestMapping("/discord")
public class DiscordController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(DiscordController.class);
    private static final long STATE_MAX_AGE_MS = 600_000L;

    @Autowired
    DiscordService discordService;

    @Autowired
    UserService userService;

    @Autowired
    TrackDevProperties properties;

    /**
     * Initiates the Discord OAuth2 flow.
     * Generates a state parameter encoding the user ID and returns the
     * authorization URL.
     */
    @Operation(summary = "Initiate Discord handshake", description = "Get the Discord OAuth2 authorization URL", security = {
            @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/handshake")
    public ResponseEntity<Map<String, String>> handshake(Principal principal) {
        String userId = getUserId(principal);
        User user = userService.get(userId);

        if (user.getDiscordInfo() != null && user.getDiscordInfo().isLinked()) {
            throw new ControllerException(ErrorConstants.DISCORD_ALREADY_LINKED);
        }

        // Encode user ID + timestamp + signature as state
        long timestamp = System.currentTimeMillis();
        String signature = signState(userId, timestamp);
        String statePayload = userId + ":" + timestamp + ":" + signature;
        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(statePayload.getBytes(StandardCharsets.UTF_8));

        String url = discordService.buildAuthorizationUrl(state);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Handles the OAuth2 callback from Discord.
     * Public endpoint — validates state, exchanges code, links account, and
     * redirects to frontend.
     */
    @Operation(summary = "Discord OAuth Callback", description = "Handle the redirect from Discord OAuth")
    @GetMapping("/callback")
    public void callback(@Parameter(description = "OAuth authorization code") @RequestParam("code") String code,
            @Parameter(description = "State parameter with encoded user details") @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        String frontendUrl = properties.getFrontend().getUrl();

        try {
            // Decode state to extract user ID
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid state parameter");
            }
            String userId = parts[0];
            String signature = parts[2];

            // Validate state age (max 10 minutes)
            long timestamp = Long.parseLong(parts[1]);
            long ageMs = System.currentTimeMillis() - timestamp;
            if (ageMs < 0 || ageMs > STATE_MAX_AGE_MS) {
                throw new IllegalArgumentException("State parameter expired");
            }

            if (!isValidStateSignature(userId, timestamp, signature)) {
                throw new IllegalArgumentException("Invalid state signature");
            }

            User user = userService.get(userId);
            discordService.linkDiscordAccount(user, code);

            response.sendRedirect(frontendUrl + "/dashboard/settings?discord=success");
        } catch (Exception e) {
            log.error("Discord OAuth callback failed: {}", e.getMessage());
            response.sendRedirect(frontendUrl + "/dashboard/settings?discord=error");
        }
    }

    /**
     * Unlinks the Discord account from the authenticated user.
     */
    @Operation(summary = "Unlink Discord account", description = "Unlink the currently connected Discord account", security = {
            @SecurityRequirement(name = "bearerAuth") })
    @DeleteMapping("/link")
    public ResponseEntity<Void> unlinkDiscord(Principal principal) {
        String userId = getUserId(principal);
        User user = userService.get(userId);

        if (user.getDiscordInfo() == null || !user.getDiscordInfo().isLinked()) {
            throw new ControllerException(ErrorConstants.DISCORD_NOT_LINKED);
        }

        discordService.unlinkDiscordAccount(user);
        return okNoContent();
    }

    /**
     * Handles Discord Interactions Endpoint URL callbacks.
     * Public endpoint — verifies Ed25519 signature on every request,
     * responds to PING (type 1) with PONG, and handles other interaction types.
     */
    @Operation(summary = "Discord Interactions Endpoint", description = "Handle Discord interaction callbacks (slash commands, buttons, etc.)")
    @PostMapping("/interactions")
    public ResponseEntity<?> handleInteraction(
            @RequestHeader(value = "X-Signature-Ed25519", required = false) String signatureHex,
            @RequestHeader(value = "X-Signature-Timestamp", required = false) String timestamp,
            @RequestBody String rawPayload) {

        // Verify Ed25519 signature
        if (signatureHex == null || timestamp == null
                || !verifyDiscordSignature(signatureHex, timestamp, rawPayload)) {
            log.warn("Discord interaction: invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid request signature");
        }

        // Parse the interaction payload
        Map<String, Object> body;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(rawPayload, Map.class);
            body = parsed;
        } catch (Exception e) {
            log.error("Discord interaction: failed to parse payload", e);
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        int type = body.get("type") instanceof Number n ? n.intValue() : 0;

        // Type 1 = PING — respond with PONG
        if (type == 1) {
            log.info("Discord interaction: PING received, responding with PONG");
            return ResponseEntity.ok(Map.of("type", 1));
        }

        // Type 2 = APPLICATION_COMMAND (slash commands)
        if (type == 2) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            String commandName = data != null ? (String) data.get("name") : "unknown";
            log.info("Discord interaction: APPLICATION_COMMAND received: {}", commandName);
            return ResponseEntity.ok(Map.of(
                    "type", 4,
                    "data", Map.of("content", "Command received: " + commandName)));
        }

        // Type 3 = MESSAGE_COMPONENT, Type 4 = AUTOCOMPLETE, Type 5 = MODAL_SUBMIT
        log.info("Discord interaction: type {} received, acknowledging", type);
        return ResponseEntity.ok(Map.of("type", 1));
    }

    /**
     * Verifies the Ed25519 signature on a Discord interaction request.
     * The message to verify is: timestamp + rawBody.
     */
    private boolean verifyDiscordSignature(String signatureHex, String timestamp, String rawBody) {
        String publicKeyHex = properties.getDiscord().getPublicKey();
        if (publicKeyHex == null || publicKeyHex.isBlank()) {
            log.error("Discord public key not configured");
            return false;
        }

        try {
            byte[] publicKeyBytes = hexToBytes(publicKeyHex);
            byte[] signatureBytes = hexToBytes(signatureHex);

            // Build the message: timestamp + body
            byte[] timestampBytes = timestamp.getBytes(StandardCharsets.UTF_8);
            byte[] bodyBytes = rawBody.getBytes(StandardCharsets.UTF_8);
            byte[] message = new byte[timestampBytes.length + bodyBytes.length];
            System.arraycopy(timestampBytes, 0, message, 0, timestampBytes.length);
            System.arraycopy(bodyBytes, 0, message, timestampBytes.length, bodyBytes.length);

            // Decode raw 32-byte Ed25519 public key into Java PublicKey
            PublicKey pubKey = decodeEd25519PublicKey(publicKeyBytes);

            Signature sig = Signature.getInstance("Ed25519");
            sig.initVerify(pubKey);
            sig.update(message);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Discord signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Decodes a raw 32-byte Ed25519 public key into a Java PublicKey.
     * Ed25519 keys are encoded as the y-coordinate in little-endian,
     * with the high bit of the last byte indicating the sign of x.
     */
    private PublicKey decodeEd25519PublicKey(byte[] rawKey) throws Exception {
        // Extract x-coordinate sign from the high bit of the last byte
        boolean xOdd = (rawKey[rawKey.length - 1] & 0x80) != 0;

        // Clear the sign bit to get pure y-coordinate bytes
        byte[] yBytes = rawKey.clone();
        yBytes[yBytes.length - 1] &= 0x7F;

        // Reverse from little-endian to big-endian for BigInteger
        byte[] reversed = new byte[yBytes.length];
        for (int i = 0; i < yBytes.length; i++) {
            reversed[i] = yBytes[yBytes.length - 1 - i];
        }
        BigInteger y = new BigInteger(1, reversed);

        EdECPublicKeySpec spec = new EdECPublicKeySpec(
                NamedParameterSpec.ED25519,
                new EdECPoint(xOdd, y));
        KeyFactory kf = KeyFactory.getInstance("EdDSA");
        return kf.generatePublic(spec);
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private boolean isValidStateSignature(String userId, long timestamp, String receivedSignature) {
        String expectedSignature = signState(userId, timestamp);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8));
    }

    private String signState(String userId, long timestamp) {
        String secret = properties.getAuth().getSecretKeyBase();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Missing auth secret key for Discord OAuth state signing");
        }

        String payload = userId + ":" + timestamp;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign Discord OAuth state", e);
        }
    }
}
