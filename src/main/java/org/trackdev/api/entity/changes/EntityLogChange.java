package org.trackdev.api.entity.changes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import org.trackdev.api.entity.BaseEntityLong;
import org.trackdev.api.entity.User;

/**
 * Base class for all entity change log entries.
 * Provides common fields: author (User who made the change), changedAt timestamp, and type discriminator.
 * Subclasses should add their own @ManyToOne relationship to the specific entity being tracked.
 */
@MappedSuperclass
public abstract class EntityLogChange extends BaseEntityLong {

    public EntityLogChange() { }

    public EntityLogChange(User author) {
        this.author = author;
        this.changedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime changedAt;

    // Used for specifications (read-only, managed by discriminator)
    @Column(name = "type", insertable = false, updatable = false)
    private String typeColumn;

    @JsonIgnore
    public User getAuthor() { return this.author; }

    /**
     * Returns the author's email for JSON serialization.
     * This avoids serializing the entire User entity in change logs.
     */
    public String getAuthorEmail() {
        try {
            return this.author != null ? this.author.getEmail() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the author's username for JSON serialization.
     * This avoids serializing the entire User entity in change logs.
     */
    public String getAuthorUsername() {
        try {
            return this.author != null ? this.author.getUsername() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public ZonedDateTime getChangedAt() { return this.changedAt; }

    public abstract String getType();
}