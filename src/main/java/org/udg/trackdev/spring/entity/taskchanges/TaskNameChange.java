package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskNameChange.CHANGE_TYPE_NAME)
public class TaskNameChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "name_change";

    public TaskNameChange() {}

    public TaskNameChange(User author, Task task, String name) {
        super(author, task);
        this.name = name;
    }

    private String name;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public String getName() { return this.name; }
}