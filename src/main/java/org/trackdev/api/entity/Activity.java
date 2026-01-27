package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Activity entity for tracking task-related events for student notifications.
 * Stores events like task creation, status changes, PR changes, etc.
 */
@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_project", columnList = "project_id"),
    @Index(name = "idx_activity_created_at", columnList = "created_at"),
    @Index(name = "idx_activity_user", columnList = "user_id")
})
public class Activity extends BaseEntityLong {

    public static final int MESSAGE_LENGTH = 500;

    public Activity() {}

    public Activity(ActivityType type, User actor, Project project, Task task, String message) {
        this.type = type;
        this.actor = actor;
        this.project = project;
        this.task = task;
        this.message = message;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(length = MESSAGE_LENGTH)
    private String message;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    // Getters
    public ActivityType getType() { return type; }
    public User getActor() { return actor; }
    public Project getProject() { return project; }
    public Task getTask() { return task; }
    public String getMessage() { return message; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public ZonedDateTime getCreatedAt() { return createdAt; }

    // Derived getters for JSON serialization
    public String getActorId() {
        return actor != null ? actor.getId() : null;
    }

    public String getActorUsername() {
        return actor != null ? actor.getUsername() : null;
    }

    public String getActorFullName() {
        return actor != null ? actor.getFullName() : null;
    }

    public String getActorEmail() {
        return actor != null ? actor.getEmail() : null;
    }

    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    public String getProjectName() {
        return project != null ? project.getName() : null;
    }

    public Long getTaskId() {
        return task != null ? task.getId() : null;
    }

    public String getTaskKey() {
        return task != null ? task.getTaskKey() : null;
    }

    public String getTaskName() {
        return task != null ? task.getName() : null;
    }

    // Setters
    public void setType(ActivityType type) { this.type = type; }
    public void setActor(User actor) { this.actor = actor; }
    public void setProject(Project project) { this.project = project; }
    public void setTask(Task task) { this.task = task; }
    public void setMessage(String message) { this.message = message; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
