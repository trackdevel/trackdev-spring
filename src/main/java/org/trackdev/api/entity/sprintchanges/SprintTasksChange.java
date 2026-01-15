package org.trackdev.api.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public abstract class SprintTasksChange extends SprintChange {

    public SprintTasksChange() {}

    public SprintTasksChange(User author, Sprint sprint, Task task) {
        super(author, sprint);
        this.task = task;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @JsonIgnore
    public Task getTask() {
        return this.task;
    }

    public Long getTaskId() {
        try {
            return this.task != null ? this.task.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
