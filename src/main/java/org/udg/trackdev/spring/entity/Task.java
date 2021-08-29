package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.controller.exceptions.EntityException;
import org.udg.trackdev.spring.entity.taskchanges.TaskChange;
import org.udg.trackdev.spring.entity.taskchanges.TaskStatusChange;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;
import org.udg.trackdev.spring.serializer.JsonHierarchyViewSerializer;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "tasks")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class Task extends BaseEntityLong {

    public static final int NAME_LENGTH = 100;

    public Task() {}

    public Task(String name, User reporter) {
        this.name = name;
        this.createdAt = new Date();
        this.reporter = reporter;
        this.status = TaskStatus.CREATED;
    }

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @ManyToOne
    @JoinColumn(name = "backlogId")
    private Backlog backlog;

    @Column(name = "backlogId", insertable = false, updatable = false)
    private Long backlogId;

    @ManyToOne
    private User reporter;

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

    @OneToMany(mappedBy = "task")
    private Collection<PullRequest> pullRequests;

    @ManyToOne
    @JoinColumn(name = "activeSprintId")
    private Sprint activeSprint;

    @Column(name = "activeSprintId", insertable = false, updatable = false)
    private Long activeSprintId;

    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL)
    private Collection<TaskChange> taskChanges;

    @NonNull
    @JsonView(EntityLevelViews.Basic.class)
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getCreatedAt() { return createdAt; }

    @JsonView(EntityLevelViews.Basic.class)
    public User getReporter() { return reporter; }

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class } )
    @JsonSerialize(using = JsonHierarchyViewSerializer.class)
    public Backlog getBacklog() {
        return backlog;
    }

    public void setBacklog(Backlog backlog) {
        this.backlog = backlog;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public User getAssignee() { return assignee; }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus status, User modifier) {
        this.status = status;
        this.taskChanges.add(new TaskStatusChange(modifier, this, status));
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

    public Collection<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public Sprint getActiveSprint() {
        return activeSprint;
    }

    public void setActiveSprint(Sprint activeSprint) {
        if(activeSprint != null && activeSprint.getBacklog() != this.backlog) {
            throw new EntityException("Cannot active sprint to task because they belong to different backlogs");
        }
        this.activeSprint = activeSprint;
    }
}
