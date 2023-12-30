package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntityLong {

    public static final int MAX_LENGTH = 1000;

    @JsonView(EntityLevelViews.Basic.class)
    @Column(length = MAX_LENGTH)
    private String content;

    @ManyToOne
    @JoinColumn(name = "authorId")
    @JsonView(EntityLevelViews.Basic.class)
    private User author;

    @ManyToOne
    @JoinColumn(name = "taskId")
    private Task task;

    @JsonView(EntityLevelViews.Basic.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date date;

    public Comment() {}

    public Comment(String content, User author, Task task) {
        this.content = content;
        this.author = author;
        this.task = task;
        this.date = new Date();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User authorId) {
        this.author = authorId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
