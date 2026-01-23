package org.trackdev.api.entity.prchanges;

import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Records when new commits are pushed to a Pull Request (synchronize event).
 */
@Entity
@DiscriminatorValue(value = PullRequestSynchronizeChange.CHANGE_TYPE_NAME)
public class PullRequestSynchronizeChange extends PullRequestChange {
    public static final String CHANGE_TYPE_NAME = "pr_synchronize";

    public PullRequestSynchronizeChange() {}

    public PullRequestSynchronizeChange(User author, PullRequest pullRequest, String githubUser) {
        super(author, pullRequest, githubUser);
    }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
