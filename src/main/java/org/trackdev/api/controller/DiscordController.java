package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
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
