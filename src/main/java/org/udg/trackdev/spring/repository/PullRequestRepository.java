package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.PullRequest;

import java.util.Optional;

@Component
public interface PullRequestRepository extends BaseRepositoryUUID<PullRequest> {

    Optional<PullRequest> findByNodeId(String nodeId);
}
