package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.BaseEntityLong;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.Global;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_changes")
public abstract class TaskChange extends BaseEntityLong {

    public TaskChange() { }

    public TaskChange(User author, Task task) {
        this.author = author;
        this.task = task;
        this.changedAt = LocalDateTime.now();
    }

    @ManyToOne
    private User author;

    @ManyToOne
    private Task task;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime changedAt;

    @JsonView(EntityLevelViews.Basic.class)
    public User getAuthor() { return this.author; }

    @JsonIgnore
    public Task getTask() { return this.task; }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_DATE_FORMAT)
    public LocalDateTime getChangedAt() { return this.changedAt; }
}
