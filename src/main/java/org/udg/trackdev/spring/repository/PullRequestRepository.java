package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.PullRequest;

import java.util.Optional;

@Component
public interface PullRequestRepository extends BaseRepositoryLong<PullRequest> {

    Optional<PullRequest> findByPrNodeId(String prNodeId);
}
