package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.controller.exceptions.EntityException;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "sprints")
public class Sprint extends BaseEntityLong {

    public Sprint() {}

    public Sprint(String name) {
        this.name = name;
    }

    @NonNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "backlogId")
    private Backlog backlog;

    @Column(name = "backlogId", insertable = false, updatable = false)
    private Long backlogId;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date startDate;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date endDate;

    @OneToMany(mappedBy = "activeSprint")
    private Collection<Task> activeTasks;

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
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Collection<Task> getActiveTasks() {
        return activeTasks;
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
}
