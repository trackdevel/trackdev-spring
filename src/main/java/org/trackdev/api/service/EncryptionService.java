package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data using AES-GCM.
 * The encryption key is loaded from the ENCRYPTION_KEY environment variable.
 * 
 * AES-GCM provides both confidentiality and integrity protection.
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;
    private final boolean encryptionEnabled;

    public EncryptionService(@Value("${ENCRYPTION_KEY:}") String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            logger.warn("ENCRYPTION_KEY not set. Encryption is DISABLED. Set ENCRYPTION_KEY environment variable for production use.");
            this.secretKey = null;
            this.encryptionEnabled = false;
        } else {
            // Key should be 32 bytes (256 bits) for AES-256
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                // Pad with zeros if key is too short (not recommended for production)
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
                logger.warn("ENCRYPTION_KEY is shorter than 32 bytes. Key will be padded. Use a 32-byte key for production.");
            } else if (keyBytes.length > 32) {
                // Truncate if key is too long
                byte[] truncatedKey = new byte[32];
                System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);
                keyBytes = truncatedKey;
            }
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            this.encryptionEnabled = true;
            logger.info("Encryption service initialized with AES-256-GCM");
        }
    }

    /**
     * Check if encryption is enabled.
     */
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    /**
     * Encrypt a plaintext string.
     * Returns the original value if encryption is disabled.
     * 
     * @param plaintext The string to encrypt
     * @return Base64-encoded ciphertext (IV prepended) or original value if encryption disabled
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        if (!encryptionEnabled) {
            return plaintext;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Return as Base64 with prefix to identify encrypted values
            return "ENC:" + Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a ciphertext string.
     * Returns the original value if it's not encrypted or encryption is disabled.
     * 
     * @param ciphertext Base64-encoded ciphertext with "ENC:" prefix
     * @return Decrypted plaintext
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        
        // Check if value is actually encrypted
        if (!ciphertext.startsWith("ENC:")) {
            // Not encrypted, return as-is (for backwards compatibility)
            return ciphertext;
        }

        if (!encryptionEnabled) {
            // Encryption disabled but value is encrypted - this is a configuration error
            logger.error("Found encrypted value but ENCRYPTION_KEY is not set. Cannot decrypt.");
            throw new RuntimeException("Cannot decrypt: ENCRYPTION_KEY not configured");
        }

        try {
            // Remove prefix and decode Base64
            String base64Data = ciphertext.substring(4);
            byte[] decoded = Base64.getDecoder().decode(base64Data);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] plaintext = cipher.doFinal(encryptedData);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
