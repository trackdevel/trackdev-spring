package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskRankChange.CHANGE_TYPE_NAME)
public class TaskRankChange extends TaskChange {

    public static final String CHANGE_TYPE_NAME = "rank_change";

    public TaskRankChange() { }

    public TaskRankChange(String user, Long taskId, Integer oldValue, Integer newValue) {
        super(user, taskId);
        this.oldValue = oldValue.toString();
        this.newValue = newValue.toString();
    }

    private String oldValue;

    private String newValue;

    @JsonView(EntityLevelViews.Basic.class)
    public String getOldValue() {
        return this.oldValue;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public String getNewValue() {
        return this.newValue;
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
