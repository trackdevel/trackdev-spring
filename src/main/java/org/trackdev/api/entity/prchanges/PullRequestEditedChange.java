package org.trackdev.api.entity.prchanges;

import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Records when a Pull Request is edited (title, body, etc. changed).
 */
@Entity
@DiscriminatorValue(value = PullRequestEditedChange.CHANGE_TYPE_NAME)
public class PullRequestEditedChange extends PullRequestChange {
    public static final String CHANGE_TYPE_NAME = "pr_edited";

    public PullRequestEditedChange() {}

    public PullRequestEditedChange(User author, PullRequest pullRequest, String githubUser, String newTitle) {
        super(author, pullRequest, githubUser);
        this.newTitle = newTitle;
    }

    /**
     * The new title after editing (if title was changed)
     */
    @Column(length = 255)
    private String newTitle;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public String getNewTitle() {
        return newTitle;
    }
}
