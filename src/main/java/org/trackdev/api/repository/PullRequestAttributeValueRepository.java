package org.trackdev.api.repository;

import org.trackdev.api.entity.PullRequestAttributeValue;

import java.util.List;
import java.util.Optional;

public interface PullRequestAttributeValueRepository extends BaseRepositoryLong<PullRequestAttributeValue> {

    List<PullRequestAttributeValue> findByPullRequestId(String pullRequestId);

    Optional<PullRequestAttributeValue> findByPullRequestIdAndAttributeId(String pullRequestId, Long attributeId);

    void deleteByPullRequestIdAndAttributeId(String pullRequestId, Long attributeId);

    void deleteByPullRequestId(String pullRequestId);

    boolean existsByAttributeId(Long attributeId);

    boolean existsByAttributeIdAndValue(Long attributeId, String value);
}
