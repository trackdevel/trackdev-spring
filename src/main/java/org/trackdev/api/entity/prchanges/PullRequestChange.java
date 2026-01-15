package org.trackdev.api.entity.prchanges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.changes.EntityLogChange;

import jakarta.persistence.*;

/**
 * Base class for tracking changes to Pull Requests.
 * Uses single table inheritance with discriminator column for change types.
 */
@Entity
@Table(name = "pull_request_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class PullRequestChange extends EntityLogChange {

    public PullRequestChange() {}

    public PullRequestChange(User author, PullRequest pullRequest, String githubUser) {
        super(author);
        this.pullRequest = pullRequest;
        this.githubUser = githubUser;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private PullRequest pullRequest;

    /**
     * The GitHub username who performed the action (may differ from system user)
     */
    @Column(length = 100)
    private String githubUser;

    @JsonIgnore
    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public String getPullRequestId() {
        try {
            return pullRequest != null ? pullRequest.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getGithubUser() {
        return githubUser;
    }

    public abstract String getType();
}
