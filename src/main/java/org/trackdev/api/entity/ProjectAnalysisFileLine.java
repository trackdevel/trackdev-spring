package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a single line analyzed as part of a project analysis file.
 * Stores line-level details including status (surviving, current, deleted).
 */
@Entity
@Table(name = "project_analysis_file_lines", indexes = {
    @Index(name = "idx_pafl_file", columnList = "file_id"),
    @Index(name = "idx_pafl_line_number", columnList = "line_number")
})
public class ProjectAnalysisFileLine extends BaseEntityUUID {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    @NotNull
    private ProjectAnalysisFile file;

    /**
     * Current line number (null for deleted lines)
     */
    @Column(name = "line_number")
    private Integer lineNumber;

    /**
     * Line number in the merge commit (for deleted lines)
     */
    @Column(name = "original_line_number")
    private Integer originalLineNumber;

    /**
     * Line content
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Line status: SURVIVING, CURRENT, DELETED
     */
    @Column(length = 20, nullable = false)
    @NotNull
    private String status;

    /**
     * Commit SHA that introduced this line
     */
    @Column(length = 50)
    private String commitSha;

    /**
     * URL to the commit
     */
    @Column(length = 500)
    private String commitUrl;

    /**
     * Full name of the author
     */
    @Column(length = 200)
    private String authorFullName;

    /**
     * GitHub username of the commit author
     */
    @Column(length = 100)
    private String authorGithubUsername;

    /**
     * URL to the file in the PR being analyzed
     */
    @Column(length = 500)
    private String prFileUrl;

    /**
     * PR number that originally introduced this line (for CURRENT lines)
     */
    @Column(name = "origin_pr_number")
    private Integer originPrNumber;

    /**
     * URL to the PR that originally introduced this line
     */
    @Column(name = "origin_pr_url", length = 500)
    private String originPrUrl;

    /**
     * Display order for interleaved lines
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public ProjectAnalysisFileLine() {}

    public ProjectAnalysisFileLine(ProjectAnalysisFile file, Integer displayOrder) {
        this.file = file;
        this.displayOrder = displayOrder;
    }

    // Getters and setters
    public ProjectAnalysisFile getFile() { return file; }
    public void setFile(ProjectAnalysisFile file) { this.file = file; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public Integer getOriginalLineNumber() { return originalLineNumber; }
    public void setOriginalLineNumber(Integer originalLineNumber) { this.originalLineNumber = originalLineNumber; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }

    public String getCommitUrl() { return commitUrl; }
    public void setCommitUrl(String commitUrl) { this.commitUrl = commitUrl; }

    public String getAuthorFullName() { return authorFullName; }
    public void setAuthorFullName(String authorFullName) { this.authorFullName = authorFullName; }

    public String getAuthorGithubUsername() { return authorGithubUsername; }
    public void setAuthorGithubUsername(String authorGithubUsername) { this.authorGithubUsername = authorGithubUsername; }

    public String getPrFileUrl() { return prFileUrl; }
    public void setPrFileUrl(String prFileUrl) { this.prFileUrl = prFileUrl; }

    public Integer getOriginPrNumber() { return originPrNumber; }
    public void setOriginPrNumber(Integer originPrNumber) { this.originPrNumber = originPrNumber; }

    public String getOriginPrUrl() { return originPrUrl; }
    public void setOriginPrUrl(String originPrUrl) { this.originPrUrl = originPrUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
