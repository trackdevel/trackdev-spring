package org.udg.trackdev.spring.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udg.trackdev.spring.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository extends BaseRepositoryLong<Task> {
    @Query("SELECT t FROM Task t JOIN t.backlog b WHERE b.id = :backlogId ORDER BY t.rank DESC")
    List<Task> findLastRankedOfBacklog(@Param("backlogId") Long backlogId, Pageable page);

    @Query("SELECT t FROM Task t JOIN t.backlog b WHERE b.id = :backlogId AND ( t.rank BETWEEN :fromRank AND :untilRank ) ORDER BY t.rank ASC")
    List<Task> findAllBetweenRanksOfBacklog(@Param("backlogId") Long backlogId,
                                            @Param("fromRank") Integer fromRank,
                                            @Param("untilRank") Integer untilRank);
}
