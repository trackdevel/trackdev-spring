package org.trackdev.api.entity.prchanges;

import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.User;

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

    public PullRequestReopenedChange(User author, PullRequest pullRequest, String githubUser) {
        super(author, pullRequest, githubUser);
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
