package org.trackdev.api.entity.prchanges;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Records when a Pull Request was reopened.
 */
@Entity
@DiscriminatorValue(value = PullRequestReopenedChange.CHANGE_TYPE_NAME)
public class PullRequestReopenedChange extends PullRequestChange {
    public static final String CHANGE_TYPE_NAME = "pr_reopened";

    public PullRequestReopenedChange() {}

    public PullRequestReopenedChange(String pullRequestId, String githubUser) {
        super(pullRequestId, githubUser);
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
