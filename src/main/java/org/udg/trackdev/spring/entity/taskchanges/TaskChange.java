package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.BaseEntityLong;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.Global;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
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
    @JoinColumn(name = "taskId")
    private Task task;

    // Used for specifications
    @Column(name = "taskId", insertable = false, updatable = false)
    private Long taskId;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime changedAt;

    // Used for specifications
    @Column(name = "type", insertable = false, updatable = false)
    private String type;

    @JsonView(EntityLevelViews.Basic.class)
    public User getAuthor() { return this.author; }

    @JsonIgnore
    public Task getTask() { return this.task; }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_DATE_FORMAT)
    public LocalDateTime getChangedAt() { return this.changedAt; }

    @JsonView(EntityLevelViews.Basic.class)
    public abstract String getType();
}
