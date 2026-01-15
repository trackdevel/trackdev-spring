package org.trackdev.api.entity.taskchanges;

import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskAssigneeChange.CHANGE_TYPE_NAME)
public class TaskAssigneeChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "assignee_change";

    private String oldValue;
    private String newValue;

    public TaskAssigneeChange() {}

    public TaskAssigneeChange(User author, Task task, String oldValue, String newValue) {
        super(author, task);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public String getOldValue() { return this.oldValue; }

    public String getNewValue() { return this.newValue; }
}
