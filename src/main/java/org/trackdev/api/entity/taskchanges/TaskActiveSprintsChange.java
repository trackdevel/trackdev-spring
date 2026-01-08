package org.trackdev.api.entity.taskchanges;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskActiveSprintsChange.CHANGE_TYPE_NAME)
public class TaskActiveSprintsChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "active_sprints_change";

    private String oldValue;
    private String newValue;

    public TaskActiveSprintsChange() {}

    public TaskActiveSprintsChange(String author, Long taskId, String oldValue, String newValues) {
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