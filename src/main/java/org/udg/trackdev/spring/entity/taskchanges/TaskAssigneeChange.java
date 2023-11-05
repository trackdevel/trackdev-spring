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

    private String oldValues;
    private String newValue;

    public TaskAssigneeChange() {}

    public TaskAssigneeChange(User author, Task task, String oldValues, String newValues) {
        super(author, task);
        this.oldValues = oldValues;
        this.newValue = newValues;
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public String getOldValues() { return this.oldValues; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getNewValue() { return this.newValue; }
}
