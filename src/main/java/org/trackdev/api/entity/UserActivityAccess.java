package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Tracks the last time a user accessed the activity feed.
 * Used to determine which activities are "new" (unread).
 */
@Entity
@Table(name = "user_activity_access", indexes = {
    @Index(name = "idx_user_activity_access_user", columnList = "user_id")
})
public class UserActivityAccess extends BaseEntityLong {

    public UserActivityAccess() {}

    public UserActivityAccess(User user) {
        this.user = user;
        this.lastAccessedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_accessed_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastAccessedAt;

    // Getters
    public User getUser() { return user; }
    public ZonedDateTime getLastAccessedAt() { return lastAccessedAt; }

    // Setters
    public void setUser(User user) { this.user = user; }
    public void setLastAccessedAt(ZonedDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    /**
     * Updates the last accessed timestamp to now.
     */
    public void markAsAccessed() {
        this.lastAccessedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }
}
