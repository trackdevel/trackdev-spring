package org.trackdev.api.repository;

import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.prchanges.PullRequestChange;

import java.util.List;

@Repository
public interface PullRequestChangeRepository extends BaseRepositoryLong<PullRequestChange> {

    /**
     * Find all changes for a specific pull request, ordered by date descending
     */
    List<PullRequestChange> findByPullRequestOrderByChangedAtDesc(PullRequest pullRequest);

    /**
     * Find all changes for multiple pull requests
     */
    List<PullRequestChange> findByPullRequestInOrderByChangedAtDesc(List<PullRequest> pullRequests);
}
