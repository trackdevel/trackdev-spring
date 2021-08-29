package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.controller.exceptions.EntityException;
import org.udg.trackdev.spring.entity.sprintchanges.*;
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

    public void setName(@NonNull String name, User modifier) {
        this.name = name;
        this.sprintChanges.add(new SprintNameChange(modifier, this, name));
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

    public void setStartDate(LocalDate startDate, User modifier) {
        LocalDate oldValue = this.startDate;
        this.startDate = startDate;
        if(oldValue != null) {
            sprintChanges.add(new SprintStartDateChange(modifier, this, startDate));
        }
    }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate, User modifier) {
        LocalDate oldValue = this.endDate;
        this.endDate = endDate;
        if(oldValue != null) {
            sprintChanges.add(new SprintEndDateChange(modifier, this, endDate));
        }
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

    public void addTask(Task task, User modifier) {
        if(task.getBacklog() != this.backlog) {
            throw new EntityException("Cannot add task to sprint as they belong to different backlogs");
        }
        this.activeTasks.add(task);
        this.sprintChanges.add(new SprintTaskAdded(modifier, this, task));
        if(this.status == SprintStatus.ACTIVE && task.getStatus() == TaskStatus.CREATED) {
            task.setStatus(TaskStatus.TODO, modifier);
        }
    }

    public void removeTask(Task task, User modifier) {
        this.activeTasks.remove(task);
        this.sprintChanges.add(new SprintTaskRemoved(modifier, this, task));
    }

    private boolean areAllTasksClosed() {
        boolean allClosed = this.activeTasks.stream().allMatch(
                t -> t.getStatus() == TaskStatus.DONE || t.getStatus() == TaskStatus.DELETED);
        return allClosed;
    }
}
