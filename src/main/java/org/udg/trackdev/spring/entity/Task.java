package org.udg.trackdev.spring.entity;

import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntityLong {

    public Task(String name) {
        this.name = name;
    }

    @NonNull
    private String name;

    @ManyToOne
    private Backlog backlog;

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
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

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
