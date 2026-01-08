package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.PullRequest;

import java.util.Optional;

@Component
public interface PullRequestRepository extends BaseRepositoryUUID<PullRequest> {

    Optional<PullRequest> findByNodeId(String nodeId);
    
    Optional<PullRequest> findByUrl(String url);
}
