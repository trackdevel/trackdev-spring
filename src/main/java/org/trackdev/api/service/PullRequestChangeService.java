package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.prchanges.PullRequestChange;
import org.trackdev.api.repository.PullRequestChangeRepository;

import java.util.List;

@Service
public class PullRequestChangeService extends BaseServiceLong<PullRequestChange, PullRequestChangeRepository> {

    public List<PullRequestChange> findByPullRequestOrderByChangedAtDesc(PullRequest pullRequest) {
        return repo().findByPullRequestOrderByChangedAtDesc(pullRequest);
    }

    public List<PullRequestChange> findByPullRequestInOrderByChangedAtDesc(List<PullRequest> pullRequests) {
        return repo().findByPullRequestInOrderByChangedAtDesc(pullRequests);
    }
}
