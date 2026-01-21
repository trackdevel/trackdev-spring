package org.trackdev.api.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntityLong {

    public static final int MAX_LENGTH = 1000;

    @Column(length = MAX_LENGTH)
    private String content;

    @ManyToOne
    private User author;

    @ManyToOne
    private Task task;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime date;

    public Comment() {}

    public Comment(String content, User author, Task task) {
        this.content = content;
        this.author = author;
        this.task = task;
        this.date = ZonedDateTime.now(ZoneId.of("UTC"));
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

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }
}
