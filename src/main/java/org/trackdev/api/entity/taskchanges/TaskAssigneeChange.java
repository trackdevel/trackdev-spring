package org.trackdev.api.entity.taskchanges;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskAssigneeChange.CHANGE_TYPE_NAME)
public class TaskAssigneeChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "assignee_change";

    private String oldValue;
    private String newValue;

    public TaskAssigneeChange() {}

    public TaskAssigneeChange(String author, Long taskId, String oldValue, String newValues) {
        super(author, taskId);
        this.oldValue = oldValue;
        this.newValue = newValues;
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public String getOldValue() { return this.oldValue; }

    public String getNewValue() { return this.newValue; }
}
