package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class TaskAssigneeChange extends TaskChange {

    public TaskAssigneeChange() {}

    public TaskAssigneeChange(User author, Task task, User assignee) {
        super(author, task);
        this.assignee = assignee;
    }

    @ManyToOne
    private User assignee;

    @JsonView(EntityLevelViews.Basic.class)
    public User getAssignee() { return this.assignee; }
}
