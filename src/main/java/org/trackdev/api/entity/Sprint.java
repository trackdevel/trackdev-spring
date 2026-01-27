package org.trackdev.api.entity;

import org.springframework.lang.NonNull;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "sprints")
public class Sprint extends BaseEntityLong {

    //-- ATTRIBUTES

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 50;

    @NonNull
    private String name;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime startDate;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime endDate;

    @Column(name = "`status`")
    private SprintStatus status;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "sprints_active_tasks",
        joinColumns = @JoinColumn(name = "sprint_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Collection<Task> activeTasks = new ArrayList<>();

    @ManyToOne
    private Project project;

    /**
     * Reference to the SprintPatternItem that was used to create this sprint.
     * Null if the sprint was created manually.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private SprintPatternItem sprintPatternItem;

    @Column(name = "sprint_pattern_item_id", insertable = false, updatable = false)
    private Long sprintPatternItemId;

    //--- CONSTRUCTOR

    public Sprint() {}

    public Sprint(String name) {
        this.name = name;
        this.status = SprintStatus.DRAFT;
    }

    //--- GETTERS AND SETTERS

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the stored status field (raw database value).
     * For most use cases, prefer getEffectiveStatus() which computes from dates.
     */
    public SprintStatus getStatus() { return this.status; }

    /**
     * Computes the effective status based on dates and any manual overrides.
     * Rules:
     * 1. If manually set to CLOSED, always return CLOSED (manual close sticks)
     * 2. Otherwise compute from dates:
     *    - Before startDate → DRAFT
     *    - Between startDate and endDate (inclusive) → ACTIVE
     *    - After endDate → CLOSED
     * 3. If dates are null, fall back to stored status
     */
    public SprintStatus getEffectiveStatus() {
        // If manually closed, respect that
        if (this.status == SprintStatus.CLOSED) {
            return SprintStatus.CLOSED;
        }
        
        // If dates are not set, use stored status
        if (startDate == null || endDate == null) {
            return this.status;
        }
        
        ZonedDateTime now = ZonedDateTime.now();
        
        // Before start date -> DRAFT
        if (now.isBefore(startDate)) {
            return SprintStatus.DRAFT;
        }
        
        // After end date -> CLOSED
        if (now.isAfter(endDate)) {
            return SprintStatus.CLOSED;
        }
        
        // Between start and end (inclusive) -> ACTIVE
        return SprintStatus.ACTIVE;
    }

    /**
     * Returns a human-readable status text based on effective status.
     */
    public String getStatusText() { return getEffectiveStatus().toString(); }

    public void setStatus(SprintStatus status) {
        if(status == SprintStatus.ACTIVE) {
            for(Task task : this.activeTasks) {
                if(task.getStatus() == TaskStatus.BACKLOG) {
                    task.setStatus(TaskStatus.TODO);
                }
            }
        }
        this.status = status;
    }

    public Collection<Task> getActiveTasks() {
        return this.activeTasks;
    }

    public void setActiveTasks(Collection<Task> tasks) {
        this.activeTasks = tasks;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public SprintPatternItem getSprintPatternItem() {
        return sprintPatternItem;
    }

    public void setSprintPatternItem(SprintPatternItem sprintPatternItem) {
        this.sprintPatternItem = sprintPatternItem;
    }

    public Long getSprintPatternItemId() {
        return sprintPatternItemId;
    }

    //--- METHODS

    public void addTask(Task task, User modifier) {
        this.activeTasks.add(task);
        // When adding task to an active sprint, change from BACKLOG to TODO
        if(this.getEffectiveStatus() == SprintStatus.ACTIVE && task.getStatus() == TaskStatus.BACKLOG) {
            task.setStatus(TaskStatus.TODO);
        }
    }

    public void removeTask(Task task) {
        this.activeTasks.remove(task);
    }

    private boolean areAllTasksClosed() {
        boolean allClosed = this.activeTasks.stream().allMatch(
                t -> t.getStatus() == TaskStatus.DONE);
        return allClosed;
    }
}
