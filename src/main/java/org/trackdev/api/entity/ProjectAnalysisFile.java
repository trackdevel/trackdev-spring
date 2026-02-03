package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a file analyzed as part of a project analysis.
 * Stores file-level summary of surviving/deleted lines.
 */
@Entity
@Table(name = "project_analysis_files", indexes = {
    @Index(name = "idx_paf_analysis", columnList = "analysis_id"),
    @Index(name = "idx_paf_sprint", columnList = "sprint_id"),
    @Index(name = "idx_paf_author", columnList = "author_id")
})
public class ProjectAnalysisFile extends BaseEntityUUID {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @NotNull
    private ProjectAnalysis analysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pr_id", nullable = false)
    @NotNull
    private PullRequest pullRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(length = 500, nullable = false)
    @NotNull
    private String filePath;

    /**
     * File status: added, modified, deleted, renamed
     */
    @Column(length = 20)
    private String status;

    /**
     * Lines added in the PR (from GitHub API)
     */
    private Integer additions = 0;

    /**
     * Lines deleted in the PR (from GitHub API)
     */
    private Integer deletions = 0;

    /**
     * Lines from this PR that still exist in current code
     */
    private Integer survivingLines = 0;

    /**
     * Lines from this PR that have been modified/deleted since merge
     */
    private Integer deletedLines = 0;

    /**
     * Total lines in current file (for context)
     */
    private Integer currentLines = 0;

    public ProjectAnalysisFile() {}

    public ProjectAnalysisFile(ProjectAnalysis analysis, PullRequest pullRequest, String filePath) {
        this.analysis = analysis;
        this.pullRequest = pullRequest;
        this.filePath = filePath;
    }

    // Getters and setters
    public ProjectAnalysis getAnalysis() { return analysis; }
    public void setAnalysis(ProjectAnalysis analysis) { this.analysis = analysis; }

    public PullRequest getPullRequest() { return pullRequest; }
    public void setPullRequest(PullRequest pullRequest) { this.pullRequest = pullRequest; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Sprint getSprint() { return sprint; }
    public void setSprint(Sprint sprint) { this.sprint = sprint; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAdditions() { return additions; }
    public void setAdditions(Integer additions) { this.additions = additions; }

    public Integer getDeletions() { return deletions; }
    public void setDeletions(Integer deletions) { this.deletions = deletions; }

    public Integer getSurvivingLines() { return survivingLines; }
    public void setSurvivingLines(Integer survivingLines) { this.survivingLines = survivingLines; }

    public Integer getDeletedLines() { return deletedLines; }
    public void setDeletedLines(Integer deletedLines) { this.deletedLines = deletedLines; }

    public Integer getCurrentLines() { return currentLines; }
    public void setCurrentLines(Integer currentLines) { this.currentLines = currentLines; }

    /**
     * Get survival rate as percentage
     */
    public double getSurvivalRate() {
        int total = (survivingLines != null ? survivingLines : 0) + (deletedLines != null ? deletedLines : 0);
        if (total == 0) return 100.0;
        return (survivingLines != null ? survivingLines : 0) * 100.0 / total;
    }
}
