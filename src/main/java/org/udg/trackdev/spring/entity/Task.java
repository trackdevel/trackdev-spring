package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.controller.exceptions.EntityException;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "tasks")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class Task extends BaseEntityLong {

    public static final int NAME_LENGTH = 100;

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

    @NonNull
    @JsonView(EntityLevelViews.Basic.class)
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getType() {
        return type.toString();
    }

    @NonNull
    @JsonView(EntityLevelViews.Basic.class)
    public void setType(TaskType type) {
        this.type = type;
    }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getCreatedAt() { return createdAt; }

    @JsonView(EntityLevelViews.Basic.class)
    public User getReporter() { return reporter; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    @JsonView({EntityLevelViews.TaskWithProjectMembers.class} )
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public User getAssignee() { return assignee; }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public TaskStatus getStatus() { return status; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getStatusText() { return status.toString(); }

    public void setStatus(TaskStatus status) {
        checkCanMoveToStatus(status);
        this.status = status;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Integer getEstimationPoints() { return estimationPoints; }

    public void setEstimationPoints(Integer estimation) {
        this.estimationPoints = estimation;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Integer getRank() { return this.rank; }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Collection<Task> getChildTasks() {
        return childTasks;
    }

    public void addChildTask(Task task) { this.childTasks.add(task); }

    @JsonView(EntityLevelViews.Basic.class)
    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    @JsonView(EntityLevelViews.TaskComplete.class)
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


    @JsonView(EntityLevelViews.TaskComplete.class)
    public Collection<Sprint> getActiveSprints() {
        return activeSprints;
    }

    public void setActiveSprints(Collection<Sprint> activeSprints) {
        this.activeSprints = activeSprints;
    }

    private void checkCanMoveToStatus(TaskStatus status) {
        if(this.status == TaskStatus.BACKLOG && status != TaskStatus.TODO) {
            throw new EntityException(String.format("Cannot change status from CREATED to new status <%s>", status));
        }
        if(status == TaskStatus.BACKLOG) {
            throw new EntityException("Cannot set status to CREATED");
        }
    }
}
