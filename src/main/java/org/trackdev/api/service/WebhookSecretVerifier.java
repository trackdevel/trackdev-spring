package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates webhook secret verification via GitHub ping events.
 *
 * Flow:
 * 1. Service calls startVerification(repoId, candidateSecret)
 * 2. Service triggers a GitHub ping via API
 * 3. HookController receives the ping and calls handlePing(repoId, payload, signature)
 * 4. This class verifies the signature against the candidate secret
 * 5. The CompletableFuture completes with true (match) or false (mismatch)
 * 6. Service reads the result and saves or rejects the secret
 */
@Component
public class WebhookSecretVerifier {

    private static final Logger log = LoggerFactory.getLogger(WebhookSecretVerifier.class);

    private final ConcurrentHashMap<Long, PendingVerification> pending = new ConcurrentHashMap<>();

    /**
     * Start a verification for the given repo. Returns a future that completes
     * when the ping arrives (true = signature matches, false = mismatch).
     */
    public CompletableFuture<Boolean> startVerification(Long repoId, String candidateSecret) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pending.put(repoId, new PendingVerification(candidateSecret, future));
        log.debug("Started webhook secret verification for repo {}", repoId);
        return future;
    }

    /**
     * Called by HookController when a ping event arrives.
     * Returns true if this repo had a pending verification (handled), false otherwise.
     */
    public boolean handlePing(Long repoId, String rawPayload, String signature) {
        PendingVerification verification = pending.get(repoId);
        if (verification == null) {
            return false;
        }

        boolean valid = verifySignature(rawPayload, signature, verification.secret);
        verification.future.complete(valid);
        log.info("Webhook secret verification for repo {}: {}", repoId, valid ? "SUCCESS" : "FAILED");
        return true;
    }

    /**
     * Cancel and clean up a pending verification.
     */
    public void cancelVerification(Long repoId) {
        PendingVerification removed = pending.remove(repoId);
        if (removed != null && !removed.future.isDone()) {
            removed.future.complete(false);
        }
    }

    /**
     * Verify a GitHub webhook signature using HMAC-SHA256.
     */
    static boolean verifySignature(String payload, String signature, String secret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }

        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder("sha256=");
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return MessageDigest.isEqual(
                    sb.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to verify webhook signature", e);
            return false;
        }
    }

    private static class PendingVerification {
        final String secret;
        final CompletableFuture<Boolean> future;

        PendingVerification(String secret, CompletableFuture<Boolean> future) {
            this.secret = secret;
            this.future = future;
        }
    }
}
