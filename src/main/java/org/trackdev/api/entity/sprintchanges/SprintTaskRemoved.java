package org.trackdev.api.entity.sprintchanges;

import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = SprintTaskRemoved.CHANGE_TYPE_NAME)
public class SprintTaskRemoved extends SprintTasksChange {
    public static final String CHANGE_TYPE_NAME = "task_removed";

    public SprintTaskRemoved() {}

    public SprintTaskRemoved(User author, Sprint sprint, Task task) {
        super(author, sprint, task);
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
