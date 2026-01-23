package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Entity to store password reset tokens.
 * Tokens are single-use and expire after a configurable time period.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends BaseEntityUUID {

    public static final int TOKEN_LENGTH = 64;
    public static final int TOKEN_VALIDITY_HOURS = 24;

    @NotNull
    @Column(unique = true, length = TOKEN_LENGTH)
    private String token;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime expiresAt;

    @NotNull
    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    @NotNull
    private Boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, User user, ZonedDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = ZonedDateTime.now();
        this.used = false;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
