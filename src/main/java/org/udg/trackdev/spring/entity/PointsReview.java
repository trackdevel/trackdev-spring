package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;

@Entity
@Table(name = "points_reviews")
public class PointsReview extends BaseEntityLong {


    @Min(0)
    private Integer points;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public PointsReview() {}

    public PointsReview(Integer points, String comment, Task task, User user) {
        this.points = points;
        this.comment = comment;
        this.task = task;
        this.user = user;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Integer getPoints() { return points; }

    public void setPoints(Integer points) { this.points = points; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public Task getTask() { return task; }

    public void setTask(Task task) { this.task = task; }

    @JsonView(EntityLevelViews.Basic.class)
    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
