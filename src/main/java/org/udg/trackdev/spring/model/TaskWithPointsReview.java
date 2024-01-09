package org.udg.trackdev.spring.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.PointsReview;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import java.util.List;

public class TaskWithPointsReview {

    @JsonView(EntityLevelViews.TaskComplete.class)
    public Task task;

    @JsonView(EntityLevelViews.TaskComplete.class)
    public List<PointsReview> pointsReview;

    public TaskWithPointsReview(Task task, List<PointsReview> pointsReviewList) {
        this.task = task;
        this.pointsReview = pointsReviewList;
    }
}
