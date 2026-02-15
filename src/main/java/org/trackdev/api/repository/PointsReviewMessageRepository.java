package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.PointsReviewConversation;
import org.trackdev.api.entity.PointsReviewMessage;

import java.util.List;

@Component
public interface PointsReviewMessageRepository extends BaseRepositoryLong<PointsReviewMessage> {
    List<PointsReviewMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    void deleteByConversation(PointsReviewConversation conversation);
}
