package org.udg.trackdev.spring.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "pull_requests")
public class PullRequest extends BaseEntityUUID {

    public PullRequest() {}

    public PullRequest(String url, String nodeId) {
        this.url = url;
        this.nodeId = nodeId;
    }

    @Column(length = 32)
    @NotNull
    private String nodeId;

    @ManyToOne
    private Task task;

    @NotNull
    private String url;

    @NotNull
    @ManyToOne
    private User author;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
