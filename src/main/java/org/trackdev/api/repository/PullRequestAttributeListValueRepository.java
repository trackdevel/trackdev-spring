package org.trackdev.api.repository;

import org.trackdev.api.entity.PullRequestAttributeListValue;

import java.util.List;

public interface PullRequestAttributeListValueRepository extends BaseRepositoryLong<PullRequestAttributeListValue> {

    List<PullRequestAttributeListValue> findByPullRequestIdAndAttributeIdOrderByOrderIndex(String pullRequestId, Long attributeId);

    List<PullRequestAttributeListValue> findByPullRequestId(String pullRequestId);

    void deleteByPullRequestIdAndAttributeId(String pullRequestId, Long attributeId);

    void deleteByPullRequestId(String pullRequestId);

    boolean existsByAttributeId(Long attributeId);

    boolean existsByAttributeIdAndEnumValue(Long attributeId, String enumValue);
}
