package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = "points_review_conversations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "initiator_id"}))
public class PointsReviewConversation extends BaseEntityLong {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Min(0)
    @Column(nullable = false)
    private Integer proposedPoints;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    private ZonedDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<PointsReviewMessage> messages = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "points_review_similar_tasks",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<Task> similarTasks = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "points_review_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    public PointsReviewConversation() {}

    public PointsReviewConversation(Task task, User initiator, Integer proposedPoints) {
        this.task = task;
        this.initiator = initiator;
        this.proposedPoints = proposedPoints;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.updatedAt = this.createdAt;
    }

    // Getters and setters

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getInitiator() { return initiator; }
    public void setInitiator(User initiator) { this.initiator = initiator; }

    public Integer getProposedPoints() { return proposedPoints; }
    public void setProposedPoints(Integer proposedPoints) {
        this.proposedPoints = proposedPoints;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PointsReviewMessage> getMessages() { return messages; }

    public void addMessage(PointsReviewMessage message) {
        this.messages.add(message);
        message.setConversation(this);
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public Set<Task> getSimilarTasks() { return similarTasks; }
    public void setSimilarTasks(Set<Task> similarTasks) {
        this.similarTasks = similarTasks;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public Set<User> getParticipants() { return participants; }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public boolean isParticipant(String userId) {
        return participants.stream().anyMatch(u -> u.getId().equals(userId));
    }

    public boolean isInitiator(String userId) {
        return initiator != null && initiator.getId().equals(userId);
    }

    /**
     * Check if a user has access to this conversation.
     * Returns true for initiator or added participants.
     * Does NOT check for professor/admin - that should be done separately.
     */
    public boolean hasAccess(String userId) {
        return isInitiator(userId) || isParticipant(userId);
    }

    public int getMessageCount() {
        return messages.size();
    }

    public ZonedDateTime getLastMessageAt() {
        if (messages.isEmpty()) return createdAt;
        return messages.get(messages.size() - 1).getCreatedAt();
    }
}
