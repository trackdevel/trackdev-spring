package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * All conversations a user can view: they are the initiator, a participant,
     * or own the course / subject the task belongs to. Used to power the
     * "active points reviews" overview. Ordered most-recently-updated first.
     */
    @Query("SELECT DISTINCT c FROM PointsReviewConversation c " +
           "LEFT JOIN c.participants p " +
           "WHERE c.initiator.id = :userId " +
           "   OR p.id = :userId " +
           "   OR c.task.project.course.owner.id = :userId " +
           "   OR c.task.project.course.subject.owner.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<PointsReviewConversation> findVisibleToUser(@Param("userId") String userId);

    /**
     * All conversations (admin view). Ordered most-recently-updated first.
     */
    @Query("SELECT c FROM PointsReviewConversation c ORDER BY c.updatedAt DESC")
    List<PointsReviewConversation> findAllOrderByUpdatedAtDesc();
}
