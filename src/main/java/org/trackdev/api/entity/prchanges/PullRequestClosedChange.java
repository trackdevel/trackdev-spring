package org.trackdev.api.entity.prchanges;

import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Records when a Pull Request was closed.
 * Includes whether it was merged and by whom.
 */
@Entity
@DiscriminatorValue(value = PullRequestClosedChange.CHANGE_TYPE_NAME)
public class PullRequestClosedChange extends PullRequestChange {
    public static final String CHANGE_TYPE_NAME = "pr_closed";

    public PullRequestClosedChange() {}

    public PullRequestClosedChange(User author, PullRequest pullRequest, String githubUser, 
                                    Boolean merged, String mergedBy) {
        super(author, pullRequest, githubUser);
        this.merged = merged;
        this.mergedBy = mergedBy;
    }

    /**
     * Whether the PR was merged (true) or just closed without merge (false)
     */
    private Boolean merged;

    /**
     * GitHub username of who merged the PR (if merged)
     */
    @Column(length = 100)
    private String mergedBy;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public Boolean getMerged() {
        return merged;
    }

    public boolean isMerged() {
        return Boolean.TRUE.equals(merged);
    }

    public String getMergedBy() {
        return mergedBy;
    }
}
