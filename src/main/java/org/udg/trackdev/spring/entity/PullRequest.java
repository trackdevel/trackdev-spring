package org.udg.trackdev.spring.entity;

import javax.persistence.*;

@Entity
@Table(name = "pull_requests")
public class PullRequest extends BaseEntityLong {

    public PullRequest() {}

    public PullRequest(String url, String prNodeId) {
        this.url = url;
        this.prNodeId = prNodeId;
    }

    @ManyToOne
    private Task task;

    private String url;

    private String prNodeId;

    @ManyToOne
    private User author;

    public void setTask(Task task) {
        this.task = task;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
