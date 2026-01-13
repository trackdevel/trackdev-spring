package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.trackdev.api.configuration.DateFormattingConfiguration;
import org.trackdev.api.serializer.JsonDateSerializer;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "sprints")
public class Sprint extends BaseEntityLong {

    //-- ATTRIBUTES

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 50;

    @NonNull
    private String name;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date startDate;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date endDate;

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

    @JsonFormat(pattern = DateFormattingConfiguration.SIMPLE_LOCALDATE_FORMAT)
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonFormat(pattern = DateFormattingConfiguration.SIMPLE_LOCALDATE_FORMAT)
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        Date oldValue = this.endDate;
        this.endDate = endDate;
    }

    public SprintStatus getStatus() { return this.status; }

    public String getStatusText() { return this.status.toString(); }

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

    //--- METHODS

    public void addTask(Task task, User modifier) {
        this.activeTasks.add(task);
        if(this.status == SprintStatus.ACTIVE && task.getStatus() == TaskStatus.BACKLOG) {
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
