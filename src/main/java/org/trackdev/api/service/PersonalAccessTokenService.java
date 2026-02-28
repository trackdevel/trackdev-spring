package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.PersonalAccessToken;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.PersonalAccessTokenRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class PersonalAccessTokenService
        extends BaseServiceUUID<PersonalAccessToken, PersonalAccessTokenRepository> {

    private static final Logger log = LoggerFactory.getLogger(PersonalAccessTokenService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static final String TOKEN_PREFIX = "tdpat_";
    public static final int TOKEN_RANDOM_BYTES = 48;
    public static final int DISPLAY_PREFIX_LENGTH = 10;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityAuditLogger auditLogger;

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        return TOKEN_PREFIX + base64Encoder.encodeToString(randomBytes);
    }

    public static String hashToken(String plaintextToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plaintextToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Transactional
    public PersonalAccessTokenWithPlaintext createToken(String userId, String name,
                                                         ZonedDateTime expiresAt) {
        User user = userService.get(userId);

        if (expiresAt != null && expiresAt.isBefore(ZonedDateTime.now(ZoneId.of("UTC")))) {
            throw new ServiceException(ErrorConstants.PAT_EXPIRATION_IN_PAST);
        }

        String plaintextToken = generateToken();
        String tokenHash = hashToken(plaintextToken);
        String displayPrefix = plaintextToken.substring(0,
            Math.min(DISPLAY_PREFIX_LENGTH, plaintextToken.length()));

        PersonalAccessToken pat = new PersonalAccessToken(
            name, tokenHash, displayPrefix, user, expiresAt);
        repo().save(pat);

        log.info("PAT created for user: {}, name: {}", userId, name);
        auditLogger.logAdminAction(userId, "PAT_CREATED", userId, "name=" + name);

        return new PersonalAccessTokenWithPlaintext(pat, plaintextToken);
    }

    @Transactional
    public PersonalAccessToken authenticate(String plaintextToken) {
        String tokenHash = hashToken(plaintextToken);
        Optional<PersonalAccessToken> optToken = repo().findByTokenHashAndRevokedFalse(tokenHash);

        if (optToken.isEmpty()) {
            return null;
        }

        PersonalAccessToken pat = optToken.get();

        if (!pat.isValid()) {
            return null;
        }

        User user = pat.getUser();
        if (!user.getEnabled()) {
            return null;
        }

        pat.setLastUsedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        repo().save(pat);

        return pat;
    }

    @Transactional(readOnly = true)
    public List<PersonalAccessToken> listTokens(String userId) {
        User user = userService.get(userId);
        return repo().findByUserAndRevokedFalseOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void revokeToken(String tokenId, String userId) {
        PersonalAccessToken pat = get(tokenId);

        if (!pat.getUser().getId().equals(userId)) {
            throw new org.trackdev.api.controller.exceptions.SecurityException(ErrorConstants.UNAUTHORIZED);
        }

        if (pat.getRevoked()) {
            throw new ServiceException(ErrorConstants.PAT_ALREADY_REVOKED);
        }

        pat.setRevoked(true);
        repo().save(pat);

        log.info("PAT revoked for user: {}, tokenId: {}", userId, tokenId);
        auditLogger.logAdminAction(userId, "PAT_REVOKED", userId, "tokenId=" + tokenId);
    }

    @Transactional
    public void revokeAllTokensForUser(String userId) {
        User user = userService.get(userId);
        List<PersonalAccessToken> activeTokens =
            repo().findByUserAndRevokedFalseOrderByCreatedAtDesc(user);
        for (PersonalAccessToken pat : activeTokens) {
            pat.setRevoked(true);
            repo().save(pat);
        }
        if (!activeTokens.isEmpty()) {
            log.info("All PATs revoked for user: {} (count: {})", userId, activeTokens.size());
        }
    }

    public static class PersonalAccessTokenWithPlaintext {
        private final PersonalAccessToken token;
        private final String plaintextToken;

        public PersonalAccessTokenWithPlaintext(PersonalAccessToken token, String plaintextToken) {
            this.token = token;
            this.plaintextToken = plaintextToken;
        }

        public PersonalAccessToken getToken() { return token; }
        public String getPlaintextToken() { return plaintextToken; }
    }
}
