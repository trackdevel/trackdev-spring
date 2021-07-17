package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.TaskStatus;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskStatusChange.CHANGE_TYPE_NAME)
public class TaskStatusChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "status_change";

    public TaskStatusChange() {}

    public TaskStatusChange(User author, Task task, TaskStatus status) {
        super(author, task);
        this.status = status;
    }

    @Column(name = "`status`")
    private TaskStatus status;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public TaskStatus getStatus() {
        return status;
    }
}
