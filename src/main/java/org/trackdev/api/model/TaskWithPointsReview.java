package org.trackdev.api.model;

import java.util.List;

import org.trackdev.api.dto.TaskWithProjectDTO;
import org.trackdev.api.entity.PointsReview;

public class TaskWithPointsReview {

    public TaskWithProjectDTO task;

    public List<PointsReview> pointsReview;

    public TaskWithPointsReview(TaskWithProjectDTO task, List<PointsReview> pointsReviewList) {
        this.task = task;
        this.pointsReview = pointsReviewList;
    }
}
