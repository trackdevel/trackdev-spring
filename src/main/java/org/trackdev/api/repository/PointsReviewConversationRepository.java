package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.PointsReviewConversation;

import java.util.List;
import java.util.Optional;

@Component
public interface PointsReviewConversationRepository extends BaseRepositoryLong<PointsReviewConversation> {
    List<PointsReviewConversation> findByTaskId(Long taskId);
    Optional<PointsReviewConversation> findByTaskIdAndInitiatorId(Long taskId, String initiatorId);
    boolean existsByTaskIdAndInitiatorId(Long taskId, String initiatorId);
    int countByTaskId(Long taskId);
}
