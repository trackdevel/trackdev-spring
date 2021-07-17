package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = TaskAssigneeChange.CHANGE_TYPE_NAME)
public class TaskAssigneeChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "assignee_change";

    public TaskAssigneeChange() {}

    public TaskAssigneeChange(User author, Task task, User assignee) {
        super(author, task);
        this.assignee = assignee;
    }

    @ManyToOne
    private User assignee;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public User getAssignee() { return this.assignee; }
}
