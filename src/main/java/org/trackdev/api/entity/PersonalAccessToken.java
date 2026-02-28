package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Entity
@Table(name = "personal_access_tokens")
public class PersonalAccessToken extends BaseEntityUUID {

    public static final int NAME_LENGTH = 100;
    public static final int TOKEN_HASH_LENGTH = 64;
    public static final int TOKEN_PREFIX_LENGTH = 10;

    @NotNull
    @Column(length = NAME_LENGTH)
    private String name;

    @NotNull
    @Column(name = "token_hash", unique = true, length = TOKEN_HASH_LENGTH)
    private String tokenHash;

    @NotNull
    @Column(name = "token_prefix", length = TOKEN_PREFIX_LENGTH)
    private String tokenPrefix;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime expiresAt;

    @NotNull
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUsedAt;

    @NotNull
    private Boolean revoked = false;

    public PersonalAccessToken() {}

    public PersonalAccessToken(String name, String tokenHash, String tokenPrefix,
                                User user, ZonedDateTime expiresAt) {
        this.name = name;
        this.tokenHash = tokenHash;
        this.tokenPrefix = tokenPrefix;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = ZonedDateTime.now();
        this.revoked = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(ZonedDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Boolean getRevoked() { return revoked; }
    public void setRevoked(Boolean revoked) { this.revoked = revoked; }

    public boolean isExpired() {
        return expiresAt != null && ZonedDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
