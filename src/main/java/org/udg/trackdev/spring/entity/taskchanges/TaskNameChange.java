package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskNameChange.CHANGE_TYPE_NAME)
public class TaskNameChange extends TaskChange {
    public static final String CHANGE_TYPE_NAME = "name_change";

    public TaskNameChange() {}

    public TaskNameChange(String author, Long taskId, String oldValue, String newValue) {
        super(author, taskId);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    private String oldValue;

    private String newValue;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public String getOldValue() { return this.oldValue; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getNewValue() { return this.newValue; }
}