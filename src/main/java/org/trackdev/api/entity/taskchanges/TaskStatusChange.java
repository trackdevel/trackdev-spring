package org.trackdev.api.entity.taskchanges;

import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskStatusChange.CHANGE_TYPE_NAME)
public class TaskStatusChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "status_change";

    public TaskStatusChange() {}

    public TaskStatusChange(User author, Task task, String oldValue, String newValue) {
        super(author, task);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    private String oldValue;

    private String newValue;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }
}
