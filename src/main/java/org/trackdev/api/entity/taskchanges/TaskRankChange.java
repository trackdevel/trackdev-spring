package org.trackdev.api.entity.taskchanges;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskRankChange.CHANGE_TYPE_NAME)
public class TaskRankChange extends TaskChange {

    public static final String CHANGE_TYPE_NAME = "rank_change";

    public TaskRankChange() { }

    public TaskRankChange(String user, Long taskId, Integer oldValue, Integer newValue) {
        super(user, taskId);
        this.oldValue = oldValue.toString();
        this.newValue = newValue.toString();
    }

    private String oldValue;

    private String newValue;

    public String getOldValue() {
        return this.oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
