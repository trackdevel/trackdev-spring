package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

/**
 * Represents a full project analysis run.
 * Stores the status and summary of analyzing all DONE tasks' PRs in a project.
 */
@Entity
@Table(name = "project_analyses")
public class ProjectAnalysis extends BaseEntityUUID {

    public enum AnalysisStatus {
        IN_PROGRESS,
        DONE,
        FAILED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by_id", nullable = false)
    @NotNull
    private User startedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @NotNull
    private AnalysisStatus status = AnalysisStatus.IN_PROGRESS;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime startedAt;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime completedAt;

    /**
     * Total number of PRs to analyze
     */
    private Integer totalPrs = 0;

    /**
     * Number of PRs processed so far
     */
    private Integer processedPrs = 0;

    /**
     * Total files analyzed across all PRs
     */
    private Integer totalFiles = 0;

    /**
     * Total surviving lines across all files
     */
    private Integer totalSurvivingLines = 0;

    /**
     * Total deleted/non-surviving lines across all files
     */
    private Integer totalDeletedLines = 0;

    /**
     * Error message if analysis failed
     */
    @Column(length = 1000)
    private String errorMessage;

    public ProjectAnalysis() {}

    public ProjectAnalysis(Project project, User startedBy) {
        this.project = project;
        this.startedBy = startedBy;
        this.status = AnalysisStatus.IN_PROGRESS;
        this.startedAt = ZonedDateTime.now();
    }

    // Getters and setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getStartedBy() { return startedBy; }
    public void setStartedBy(User startedBy) { this.startedBy = startedBy; }

    public AnalysisStatus getStatus() { return status; }
    public void setStatus(AnalysisStatus status) { this.status = status; }

    public ZonedDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; }

    public ZonedDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getTotalPrs() { return totalPrs; }
    public void setTotalPrs(Integer totalPrs) { this.totalPrs = totalPrs; }

    public Integer getProcessedPrs() { return processedPrs; }
    public void setProcessedPrs(Integer processedPrs) { this.processedPrs = processedPrs; }

    public Integer getTotalFiles() { return totalFiles; }
    public void setTotalFiles(Integer totalFiles) { this.totalFiles = totalFiles; }

    public Integer getTotalSurvivingLines() { return totalSurvivingLines; }
    public void setTotalSurvivingLines(Integer totalSurvivingLines) { this.totalSurvivingLines = totalSurvivingLines; }

    public Integer getTotalDeletedLines() { return totalDeletedLines; }
    public void setTotalDeletedLines(Integer totalDeletedLines) { this.totalDeletedLines = totalDeletedLines; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /**
     * Mark analysis as complete
     */
    public void complete() {
        this.status = AnalysisStatus.DONE;
        this.completedAt = ZonedDateTime.now();
    }

    /**
     * Mark analysis as failed
     */
    public void fail(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.completedAt = ZonedDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * Increment processed PR count
     */
    public void incrementProcessedPrs() {
        this.processedPrs = (this.processedPrs == null ? 0 : this.processedPrs) + 1;
    }
}
