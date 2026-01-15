package org.trackdev.api.entity.taskchanges;

import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskRankChange.CHANGE_TYPE_NAME)
public class TaskRankChange extends TaskChange {

    public static final String CHANGE_TYPE_NAME = "rank_change";

    public TaskRankChange() { }

    public TaskRankChange(User author, Task task, Integer oldValue, Integer newValue) {
        super(author, task);
        this.oldValue = oldValue != null ? oldValue.toString() : null;
        this.newValue = newValue != null ? newValue.toString() : null;
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
