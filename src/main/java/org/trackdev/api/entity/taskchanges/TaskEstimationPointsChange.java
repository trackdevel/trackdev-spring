package org.trackdev.api.entity.taskchanges;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskEstimationPointsChange.CHANGE_TYPE_NAME)
public class TaskEstimationPointsChange extends TaskChange {

    public static final String CHANGE_TYPE_NAME = "estimation_points_change";

    public TaskEstimationPointsChange() { }

    public TaskEstimationPointsChange(String user, Long taskId, Integer oldValue, Integer newValue) {
        super(user, taskId);
        this.oldValue = oldValue == null ? null : oldValue.toString();
        this.newValue = newValue == null ? null : newValue.toString();
    }

    private String oldValue;

    private String newValue;

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
