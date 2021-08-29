package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.controller.exceptions.EntityException;
import org.udg.trackdev.spring.entity.sprintchanges.SprintChange;
import org.udg.trackdev.spring.entity.sprintchanges.SprintStatusChange;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.Global;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "sprints")
public class Sprint extends BaseEntityLong {

    public Sprint() {}

    public Sprint(String name) {
        this.name = name;
        this.status = SprintStatus.DRAFT;
    }

    @NonNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "backlogId")
    private Backlog backlog;

    @Column(name = "backlogId", insertable = false, updatable = false)
    private Long backlogId;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(name = "`status`")
    private SprintStatus status;

    @OneToMany(mappedBy = "activeSprint")
    private Collection<Task> activeTasks;

    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL)
    private Collection<SprintChange> sprintChanges;

    @NonNull
    @JsonView(EntityLevelViews.Basic.class)
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

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public SprintStatus getStatus() { return this.status; }

    public void setStatus(SprintStatus status, User modifier) {
        if(status == SprintStatus.CLOSED && !areAllTasksClosed()) {
            throw new EntityException("Cannot close sprint with open tasks");
        }
        if(status == SprintStatus.ACTIVE) {
            for(Task task : this.activeTasks) {
                if(task.getStatus() == TaskStatus.CREATED) {
                    task.setStatus(TaskStatus.TODO, modifier);
                }
            }
        }
        this.status = status;
        this.sprintChanges.add(new SprintStatusChange(modifier, this, status));
    }

    public Collection<Task> getActiveTasks() {
        return Collections.unmodifiableCollection(this.activeTasks);
    }

    public void addTask(Task task) {
        if(task.getBacklog() != this.backlog) {
            throw new EntityException("Cannot add task to sprint as they belong to different backlogs");
        }
        this.activeTasks.add(task);
    }

    public void removeTask(Task task) {
        this.activeTasks.remove(task);
    }

    private boolean areAllTasksClosed() {
        boolean allClosed = this.activeTasks.stream().allMatch(
                t -> t.getStatus() == TaskStatus.DONE || t.getStatus() == TaskStatus.DELETED);
        return allClosed;
    }
}
