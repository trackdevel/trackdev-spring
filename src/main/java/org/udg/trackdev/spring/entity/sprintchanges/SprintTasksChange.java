package org.udg.trackdev.spring.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public abstract class SprintTasksChange extends SprintChange {

    public SprintTasksChange() {}

    public SprintTasksChange(String author, Long sprint, Task task) {
        super(author, sprint);
        this.task = task;
    }

    @ManyToOne
    private Task task;

    @JsonView(EntityLevelViews.Basic.class)
    public Task getTask() {
        return this.task;
    }
}
