package org.trackdev.api.entity.prchanges;

import org.trackdev.api.entity.BaseEntityLong;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.trackdev.api.configuration.DateFormattingConfiguration;
import org.trackdev.api.entity.BaseEntityLong;

import java.time.LocalDateTime;

/**
 * Base class for tracking changes to Pull Requests.
 * Uses single table inheritance with discriminator column for change types.
 */
@Entity
@Table(name = "pull_request_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class PullRequestChange extends BaseEntityLong {

    public PullRequestChange() {}

    public PullRequestChange(String pullRequestId, String githubUser) {
        this.pullRequestId = pullRequestId;
        this.githubUser = githubUser;
        this.changedAt = LocalDateTime.now();
    }

    /**
     * The UUID of the PullRequest entity
     */
    @Column(length = 36)
    private String pullRequestId;

    /**
     * The GitHub username who performed the action
     */
    @Column(length = 100)
    private String githubUser;

    /**
     * When this change occurred
     */
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime changedAt;

    // Used for specifications
    @Column(name = "type", insertable = false, updatable = false)
    private String typeColumn;

    public String getPullRequestId() {
        return pullRequestId;
    }

    public String getGithubUser() {
        return githubUser;
    }

    @JsonFormat(pattern = DateFormattingConfiguration.SIMPLE_DATE_FORMAT)
    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public abstract String getType();
}
