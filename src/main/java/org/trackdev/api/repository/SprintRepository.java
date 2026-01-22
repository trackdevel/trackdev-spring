package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Sprint;

import java.util.Collection;
import java.util.List;

@Component
public interface SprintRepository extends BaseRepositoryLong<Sprint> {

    @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE end_date < sysdate() AND status != 2")
    Collection<Sprint> sprintsToClose();

   @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE start_date > sysdate() AND status = 0")
    Collection<Sprint> sprintsToDraft();

   @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE start_date < sysdate() AND end_date > sysdate() AND (status = 0 OR status = 2)")
    Collection<Sprint> sprintsToActive();

    /**
     * Find all sprints created from a specific sprint pattern item
     */
    List<Sprint> findBySprintPatternItem_Id(Long sprintPatternItemId);

    /**
     * Find all sprints for a project, ordered by order index of the pattern item
     */
    List<Sprint> findByProject_IdOrderBySprintPatternItem_OrderIndexAsc(Long projectId);

}
