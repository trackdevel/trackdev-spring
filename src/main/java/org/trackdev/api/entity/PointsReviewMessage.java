package org.trackdev.api.entity;

import jakarta.persistence.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "points_review_messages")
public class PointsReviewMessage extends BaseEntityLong {

    public static final int MAX_LENGTH = 2000;

    @Column(length = MAX_LENGTH, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private PointsReviewConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    private ZonedDateTime createdAt;

    public PointsReviewMessage() {}

    public PointsReviewMessage(String content, User author, PointsReviewConversation conversation) {
        this.content = content;
        this.author = author;
        this.conversation = conversation;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    // Getters and setters

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public PointsReviewConversation getConversation() { return conversation; }
    public void setConversation(PointsReviewConversation conversation) { this.conversation = conversation; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Check if the message is still within the 10-minute edit window.
     */
    public boolean isWithinEditWindow() {
        return ZonedDateTime.now(ZoneId.of("UTC")).isBefore(createdAt.plusMinutes(10));
    }
}
