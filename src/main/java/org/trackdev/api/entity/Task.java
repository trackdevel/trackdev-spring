package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.trackdev.api.serializer.JsonDateSerializer;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "tasks")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class Task extends BaseEntityLong {

    //-- ATTRIBUTES
    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 100;
    public static final int TASK_KEY_LENGTH = 10;

    /**
     * Unique task number within the project context (1, 2, 3, etc.)
     */
    private Integer taskNumber;

    /**
     * Unique task key combining project slug and task number (e.g., "a7k-1", "a7k-42")
     */
    @Column(length = TASK_KEY_LENGTH, unique = true)
    private String taskKey;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private Project project;

    @ManyToOne
    private User reporter;

    @Column(columnDefinition = "TEXT")
    private String description;

    private TaskType type;

    private Date createdAt;

    @ManyToOne
    private User assignee;

    private Integer estimationPoints;

    @Column(name = "`status`")
    private TaskStatus status;

    @Column(name = "`rank`")
    private Integer rank;

    @OneToMany(mappedBy = "parentTask")
    private Collection<Task> childTasks;

    @ManyToOne
    @JoinColumn(name = "parentTaskId")
    private Task parentTask;

    @Column(name = "parentTaskId", insertable = false, updatable = false)
    private Long parentTaskId;

    @ManyToMany(mappedBy = "activeTasks")
    private Collection<Sprint> activeSprints = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Comment> discussion = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointsReview> pointsReviewList = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_pull_requests",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "pull_request_id")
    )
    private Set<PullRequest> pullRequests = new HashSet<>();

    // -- CONSTRUCTORS

    public Task() {}

    public Task(String name, User reporter) {
        this.name = name;
        this.createdAt = new Date();
        this.reporter = reporter;
        this.status = TaskStatus.BACKLOG;
        this.estimationPoints = 0;
        this.rank = 0;
    }

    // -- GETTERS AND SETTERS

    public Integer getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(Integer taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getType() {
        return type != null ? type.name() : null;
    }

    public String getTypeMessageKey() {
        return type != null ? type.getMessageKey() : null;
    }

    public TaskType getTaskType() {
        return type;
    }

    @NonNull
    public void setType(TaskType type) {
        this.type = type;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getCreatedAt() { return createdAt; }

    public User getReporter() { return reporter; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignee() { return assignee; }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public TaskStatus getStatus() { return status; }

    public String getStatusText() { return status.toString(); }

    public void setStatus(TaskStatus status) {
        checkCanMoveToStatus(status);
        this.status = status;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public Integer getEstimationPoints() { return estimationPoints; }

    public void setEstimationPoints(Integer estimation) {
        this.estimationPoints = estimation;
    }

    public Integer getRank() { return this.rank; }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Collection<Task> getChildTasks() {
        return childTasks;
    }

    public void addChildTask(Task task) {
        if (this.type != TaskType.USER_STORY) {
            throw new IllegalStateException("Only tasks of type USER_STORY can have subtasks");
        }
        this.childTasks.add(task);
    }

    /**
     * Check if this task can be moved to VERIFY status.
     * Requirements:
     * - Must have at least one Pull Request associated (open or closed)
     */
    public boolean canMoveToVerify() {
        return hasPullRequest();
    }

    /**
     * Check if this task can be moved to DONE status.
     * Requirements:
     * - Must have at least one Pull Request associated
     * - All Pull Requests must be merged
     * - Must have estimation points > 0
     * - For USER_STORY: all subtasks must be DONE with estimation points and PRs
     */
    public boolean canMoveToDone() {
        // All task types require at least one Pull Request to be marked as DONE
        if (!hasPullRequest()) {
            return false;
        }
        
        // All PRs must be merged
        if (!areAllPRsMerged()) {
            return false;
        }
        
        // All task types require estimation points > 0 to be marked as DONE
        if (this.estimationPoints == null || this.estimationPoints <= 0) {
            return false;
        }
        
        if (this.type != TaskType.USER_STORY) {
            return true; // Regular tasks with estimation points and all PRs merged can move to DONE
        }
        
        // USER_STORY: check subtasks
        if (this.childTasks == null || this.childTasks.isEmpty()) {
            return true; // No subtasks, can move to DONE
        }
        
        // All subtasks must be DONE, have estimation points, and have all PRs merged
        return this.childTasks.stream().allMatch(subtask -> 
            subtask.getStatus() == TaskStatus.DONE && 
            subtask.getEstimationPoints() != null && 
            subtask.getEstimationPoints() > 0 &&
            subtask.hasPullRequest() &&
            subtask.areAllPRsMerged()
        );
    }

    /**
     * Check if this task has at least one Pull Request associated.
     */
    public boolean hasPullRequest() {
        return this.pullRequests != null && !this.pullRequests.isEmpty();
    }

    /**
     * Check if all associated Pull Requests are merged.
     * Returns true if there are no PRs (vacuously true).
     */
    public boolean areAllPRsMerged() {
        if (this.pullRequests == null || this.pullRequests.isEmpty()) {
            return true;
        }
        return this.pullRequests.stream().allMatch(PullRequest::isMerged);
    }

    /**
     * Check if all subtasks are DONE and automatically update parent status if needed.
     * This should be called after a subtask status change.
     */
    public boolean areAllSubtasksDone() {
        if (this.childTasks == null || this.childTasks.isEmpty()) {
            return true;
        }
        return this.childTasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);
    }

    public Task getParentTask() {
        return parentTask;
    }

    public Long getParentTaskId() {
        return parentTask != null ? parentTask.getId() : null;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public Collection<Comment> getDiscussion() {
        return discussion;
    }

    public List<PointsReview> getPointsReviewList() {
        return pointsReviewList;
    }

    public  void addPointsReview(PointsReview pointsReview) {
        this.pointsReviewList.add(pointsReview);
    }

    public void addComment(Comment comment) {
        this.discussion.add(comment);
        comment.setTask(this);
    }


    public Collection<Sprint> getActiveSprints() {
        return activeSprints;
    }

    public void setActiveSprints(Collection<Sprint> activeSprints) {
        this.activeSprints = activeSprints;
    }

    public Set<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public void addPullRequest(PullRequest pr) {
        this.pullRequests.add(pr);
        pr.addTask(this);
    }

    /**
     * Get the calculated estimation points for this task.
     * For USER_STORY: returns the sum of all child task estimation points.
     * For TASK/BUG: returns the manually set estimation points.
     */
    public Integer getCalculatedEstimationPoints() {
        if (this.type == TaskType.USER_STORY && this.childTasks != null && !this.childTasks.isEmpty()) {
            return this.childTasks.stream()
                .filter(subtask -> subtask.getEstimationPoints() != null)
                .mapToInt(Task::getEstimationPoints)
                .sum();
        }
        return this.estimationPoints;
    }

    private void checkCanMoveToStatus(TaskStatus status) {
        /**if(this.status == TaskStatus.BACKLOG && status != TaskStatus.TODO) {
            throw new EntityException(String.format("Cannot change status from BACKLOG to new status <%s>", status));
        }
        if(status == TaskStatus.BACKLOG) {
            throw new EntityException("Cannot set status to BACKLOG");
        }**/
    }
}
