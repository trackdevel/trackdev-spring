package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "user_push_tokens")
public class UserPushToken extends BaseEntityUUID {

    public static final int TOKEN_LENGTH = 512;
    public static final int DEVICE_ID_LENGTH = 128;

    public enum Platform {
        IOS, ANDROID, WEB
    }

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(unique = true, length = TOKEN_LENGTH)
    private String token;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Platform platform;

    @Column(name = "device_id", length = DEVICE_ID_LENGTH)
    private String deviceId;

    @NotNull
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    @NotNull
    @Column(name = "last_seen_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastSeenAt;

    public UserPushToken() {}

    public UserPushToken(User user, String token, Platform platform, String deviceId) {
        this.user = user;
        this.token = token;
        this.platform = platform;
        this.deviceId = deviceId;
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        this.createdAt = now;
        this.lastSeenAt = now;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(ZonedDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
