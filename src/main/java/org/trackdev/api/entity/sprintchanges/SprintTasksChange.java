package org.trackdev.api.entity.sprintchanges;

import org.trackdev.api.entity.Task;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public abstract class SprintTasksChange extends SprintChange {

    public SprintTasksChange() {}

    public SprintTasksChange(String author, Long sprint, Task task) {
        super(author, sprint);
        this.task = task;
    }

    @ManyToOne
    private Task task;

    public Task getTask() {
        return this.task;
    }
}
