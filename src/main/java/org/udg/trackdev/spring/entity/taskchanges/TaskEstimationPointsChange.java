package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskEstimationPointsChange.CHANGE_TYPE_NAME)
public class TaskEstimationPointsChange extends TaskChange {

    public static final String CHANGE_TYPE_NAME = "estimation_points_change";

    public TaskEstimationPointsChange() { }

    public TaskEstimationPointsChange(User user, Task task, Integer points) {
        super(user, task);
        estimationPoints = points;
    }

    private Integer estimationPoints;

    @JsonView(EntityLevelViews.Basic.class)
    public Integer getEstimationPoints() { return estimationPoints; }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
