package org.trackdev.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "points_reviews")
public class PointsReview extends BaseEntityLong {


    @Min(0)
    private Integer points;

    private String comment;

    @ManyToOne
    private Task task;

    @ManyToOne
    private User user;

    public PointsReview() {}

    public PointsReview(Integer points, String comment, Task task, User user) {
        this.points = points;
        this.comment = comment;
        this.task = task;
        this.user = user;
    }

    public Integer getPoints() { return points; }

    public void setPoints(Integer points) { this.points = points; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public Task getTask() { return task; }

    public void setTask(Task task) { this.task = task; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
