package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntityLong {

    public static final int NAME_LENGTH = 100;

    public Task() {}

    public Task(String name, User reporter) {
        this.name = name;
        this.createdAt = new Date();
        this.reporter = reporter;
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

    @OneToMany(mappedBy = "task")
    private Collection<TaskLog> taskLogs;

    @OneToMany(mappedBy = "parentTask")
    private Collection<Task> childTasks;

    @ManyToOne
    private Task parentTask;

    @OneToMany(mappedBy = "task")
    private Collection<PullRequest> pullRequests;

    @ManyToOne
    private Sprint activeSprint;

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

    public Backlog getBacklog() {
        return backlog;
    }

    public void setBacklog(Backlog backlog) {
        this.backlog = backlog;
    }

    public Collection<TaskLog> getTaskLogs() {
        return taskLogs;
    }

    public Collection<Task> getChildTasks() {
        return childTasks;
    }

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
        this.activeSprint = activeSprint;
    }
}
