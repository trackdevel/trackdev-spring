package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Entity representing a GitHub repository linked to a project.
 * Stores repository information and access token for API operations.
 */
@Entity
@Table(name = "github_repos")
public class GitHubRepo extends BaseEntityLong {

    public static final int MAX_URL_LENGTH = 500;
    public static final int MAX_NAME_LENGTH = 200;
    public static final int MAX_TOKEN_LENGTH = 500;

    @NotNull
    @Column(length = MAX_NAME_LENGTH)
    private String name;

    @NotNull
    @Column(length = MAX_URL_LENGTH)
    private String url;

    @Column(length = MAX_NAME_LENGTH)
    private String owner;

    @Column(length = MAX_NAME_LENGTH)
    private String repoName;

    @Column(length = MAX_TOKEN_LENGTH)
    private String accessToken;

    @ManyToOne
    private Project project;

    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastSyncAt;

    @Column(length = MAX_URL_LENGTH)
    private String webhookUrl;

    private Long webhookId;

    private boolean webhookActive;

    /**
     * Unique secret for this repository's webhook.
     * Used to verify webhook payloads from GitHub.
     */
    @Column(length = MAX_TOKEN_LENGTH)
    private String webhookSecret;

    // -- CONSTRUCTORS

    public GitHubRepo() {}

    public GitHubRepo(String name, String url, String accessToken, Project project) {
        this.name = name;
        this.url = url;
        this.accessToken = accessToken;
        this.project = project;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.webhookActive = false;
        parseOwnerAndRepoFromUrl(url);
    }

    // -- GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        parseOwnerAndRepoFromUrl(url);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getProjectId() {
        return projectId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(ZonedDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Long getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(Long webhookId) {
        this.webhookId = webhookId;
    }

    public boolean isWebhookActive() {
        return webhookActive;
    }

    public void setWebhookActive(boolean webhookActive) {
        this.webhookActive = webhookActive;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    // -- HELPER METHODS

    /**
     * Parse owner and repository name from GitHub URL.
     * Supports formats:
     * - https://github.com/owner/repo
     * - https://github.com/owner/repo.git
     * - git@github.com:owner/repo.git
     */
    private void parseOwnerAndRepoFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        
        String cleanUrl = url.trim();
        
        // Remove .git suffix if present
        if (cleanUrl.endsWith(".git")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
        }
        
        // Handle HTTPS URLs
        if (cleanUrl.contains("github.com/")) {
            String[] parts = cleanUrl.split("github.com/");
            if (parts.length > 1) {
                String[] ownerRepo = parts[1].split("/");
                if (ownerRepo.length >= 2) {
                    this.owner = ownerRepo[0];
                    this.repoName = ownerRepo[1];
                }
            }
        }
        // Handle SSH URLs (git@github.com:owner/repo)
        else if (cleanUrl.contains("git@github.com:")) {
            String[] parts = cleanUrl.split("git@github.com:");
            if (parts.length > 1) {
                String[] ownerRepo = parts[1].split("/");
                if (ownerRepo.length >= 2) {
                    this.owner = ownerRepo[0];
                    this.repoName = ownerRepo[1];
                }
            }
        }
    }

    /**
     * Get the full repository path (owner/repo).
     */
    public String getFullName() {
        if (owner != null && repoName != null) {
            return owner + "/" + repoName;
        }
        return null;
    }
}
