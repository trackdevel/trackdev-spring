package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pull_requests")
public class PullRequest extends BaseEntityUUID {

    public static final int MAX_URL_LENGTH = 500;
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_STATE_LENGTH = 20;

    public PullRequest() {}

    public PullRequest(String url, String nodeId) {
        this.url = url;
        this.nodeId = nodeId;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @Column(length = 32)
    @NotNull
    private String nodeId;

    @ManyToMany(mappedBy = "pullRequests", fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    @NotNull
    @Column(length = MAX_URL_LENGTH)
    private String url;

    @ManyToOne
    private User author;

    /**
     * PR number in the repository (e.g., 42 for PR #42)
     */
    private Integer prNumber;

    /**
     * PR title
     */
    @Column(length = MAX_TITLE_LENGTH)
    private String title;

    /**
     * PR state: open, closed, merged
     */
    @Column(length = MAX_STATE_LENGTH)
    private String state;

    /**
     * Repository full name (owner/repo)
     */
    @Column(length = 200)
    private String repoFullName;

    /**
     * When the PR was created on GitHub
     */
    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    /**
     * When the PR was last updated
     */
    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime updatedAt;

    /**
     * Whether the PR has been merged
     */
    private Boolean merged;

    // Getters and Setters

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public boolean hasTask(Task task) {
        return this.tasks.contains(task);
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

    public Integer getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(Integer prNumber) {
        this.prNumber = prNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public void setRepoFullName(String repoFullName) {
        this.repoFullName = repoFullName;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getMerged() {
        return merged;
    }

    public void setMerged(Boolean merged) {
        this.merged = merged;
    }

    /**
     * Check if this PR is merged.
     * A PR is considered merged if the merged field is true.
     */
    public boolean isMerged() {
        return Boolean.TRUE.equals(this.merged);
    }
}
