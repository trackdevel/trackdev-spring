package org.udg.trackdev.spring.entity.sprintchanges;

import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = SprintTaskRemoved.CHANGE_TYPE_NAME)
public class SprintTaskRemoved extends SprintTasksChange {
    public static final String CHANGE_TYPE_NAME = "task_removed";

    public SprintTaskRemoved() {}

    public SprintTaskRemoved(String author, Long sprint, Task task) {
        super(author, sprint, task);
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
