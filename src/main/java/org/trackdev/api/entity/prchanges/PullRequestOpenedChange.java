package org.trackdev.api.entity.prchanges;

import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Records when a Pull Request was opened.
 */
@Entity
@DiscriminatorValue(value = PullRequestOpenedChange.CHANGE_TYPE_NAME)
public class PullRequestOpenedChange extends PullRequestChange {
    public static final String CHANGE_TYPE_NAME = "pr_opened";

    public PullRequestOpenedChange() {}

    public PullRequestOpenedChange(User author, PullRequest pullRequest, String githubUser, 
                                    String prTitle, Integer prNumber, String repoFullName) {
        super(author, pullRequest, githubUser);
        this.prTitle = prTitle;
        this.prNumber = prNumber;
        this.repoFullName = repoFullName;
    }

    @Column(length = 255)
    private String prTitle;

    private Integer prNumber;

    @Column(length = 200)
    private String repoFullName;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public Integer getPrNumber() {
        return prNumber;
    }

    public String getRepoFullName() {
        return repoFullName;
    }
}
