package org.trackdev.api.entity.taskchanges;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskTypeChange.CHANGE_TYPE_NAME)
public class TaskTypeChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "type_change";

    public TaskTypeChange() {}

    public TaskTypeChange(String author, Long taskId, String oldValue, String newValue) {
        super(author, taskId);
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
