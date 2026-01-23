package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.PasswordResetToken;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.PasswordResetTokenRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for managing password reset tokens and password recovery.
 */
@Service
public class PasswordResetService extends BaseServiceUUID<PasswordResetToken, PasswordResetTokenRepository> {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    @Autowired
    private UserService userService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Generate a secure random token for password reset.
     * Uses URL-safe Base64 encoding.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[48]; // 384 bits of randomness
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Request a password reset for the given email.
     * Creates a token and sends an email with a reset link.
     * Always returns silently (for security - don't reveal if email exists).
     * 
     * @param email The email address to send the reset link to
     * @param language The language for the email content
     */
    @Transactional
    public void requestPasswordReset(String email, String language) {
        User user = userService.getByEmail(email);
        
        if (user == null) {
            // Don't reveal if email exists - just log and return silently
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        if (!user.getEnabled()) {
            // Don't reveal account status - just log and return silently
            log.info("Password reset requested for disabled account: {}", email);
            return;
        }

        // Invalidate any existing unused tokens for this user
        invalidateExistingTokens(user);

        // Create new token
        String token = generateSecureToken();
        ZonedDateTime expiresAt = ZonedDateTime.now(ZoneId.of("UTC"))
            .plusHours(PasswordResetToken.TOKEN_VALIDITY_HOURS);
        
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
        repo().save(resetToken);

        // Send email with reset link
        emailSenderService.sendPasswordResetEmail(email, token, language);
        
        log.info("Password reset token created for user: {}", user.getId());
    }

    /**
     * Validate a password reset token.
     * 
     * @param token The token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = repo().findByTokenAndUsedFalse(token);
        return resetToken.isPresent() && resetToken.get().isValid();
    }

    /**
     * Reset the password using a valid token.
     * 
     * @param token The reset token
     * @param newPassword The new password to set
     * @throws ServiceException if the token is invalid or expired
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = repo().findByTokenAndUsedFalse(token)
            .orElseThrow(() -> new ServiceException(ErrorConstants.INVALID_RESET_TOKEN));

        if (!resetToken.isValid()) {
            throw new ServiceException(ErrorConstants.EXPIRED_RESET_TOKEN);
        }

        User user = resetToken.getUser();
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setChangePassword(false);
        user.setRecoveryCode(null); // Clear old recovery code if any
        userService.repo().save(user);

        // Mark token as used
        resetToken.setUsed(true);
        repo().save(resetToken);

        // Invalidate any other unused tokens for this user
        invalidateExistingTokens(user);

        log.info("Password reset successful for user: {}", user.getId());
    }

    /**
     * Invalidate all existing unused tokens for a user.
     */
    @Transactional
    private void invalidateExistingTokens(User user) {
        repo().findByUserAndUsedFalse(user).forEach(token -> {
            token.setUsed(true);
            repo().save(token);
        });
    }

    /**
     * Clean up expired tokens (can be called by a scheduled job).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        repo().deleteByExpiresAtBefore(ZonedDateTime.now(ZoneId.of("UTC")));
        log.info("Cleaned up expired password reset tokens");
    }
}
