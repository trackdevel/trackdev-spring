package org.trackdev.api.entity.prchanges;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Records when a Pull Request was merged.
 */
@Entity
@DiscriminatorValue(value = PullRequestMergedChange.CHANGE_TYPE_NAME)
public class PullRequestMergedChange extends PullRequestChange {
    public static final String CHANGE_TYPE_NAME = "pr_merged";

    public PullRequestMergedChange() {}

    public PullRequestMergedChange(String pullRequestId, String githubUser, String mergedBy) {
        super(pullRequestId, githubUser);
        this.mergedBy = mergedBy;
    }

    /**
     * GitHub username of who merged the PR
     */
    @Column(length = 100)
    private String mergedBy;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public String getMergedBy() {
        return mergedBy;
    }
}
