package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Sprint;

import java.util.Collection;
import java.util.List;

@Component
public interface SprintRepository extends BaseRepositoryLong<Sprint> {

    /**
     * Remove all sprint assignments for a single task by writing only that task's
     * rows in the join table. Avoids Hibernate's collection-rewrite strategy on
     * Sprint.activeTasks, which races under concurrent task updates that target
     * the same sprint.
     */
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM sprints_active_tasks WHERE task_id = :taskId")
    void clearSprintAssignmentsForTask(@Param("taskId") Long taskId);

    /**
     * Add a single (sprint, task) row to the join table. Per-row write so two
     * concurrent task moves into the same sprint don't collide.
     */
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO sprints_active_tasks (sprint_id, task_id) VALUES (:sprintId, :taskId)")
    void addSprintAssignmentForTask(@Param("sprintId") Long sprintId, @Param("taskId") Long taskId);


    @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE end_date < sysdate() AND status != 2")
    Collection<Sprint> sprintsToClose();

   @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE start_date > sysdate() AND status = 0")
    Collection<Sprint> sprintsToDraft();

   @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE start_date < sysdate() AND end_date > sysdate() AND (status = 0 OR status = 2)")
    Collection<Sprint> sprintsToActive();

    /**
     * Find all sprints created from a specific sprint pattern item
     */
    @Query("SELECT s FROM Sprint s WHERE s.sprintPatternItem.id = :patternItemId")
    List<Sprint> findByPatternItemId(@Param("patternItemId") Long patternItemId);

    /**
     * Find all sprints for a project, ordered by order index of the pattern item
     */
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId ORDER BY s.sprintPatternItem.orderIndex ASC")
    List<Sprint> findByProjectIdOrderByPatternItemOrderIndex(@Param("projectId") Long projectId);

    /**
     * Find all sprints for a project
     */
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId")
    List<Sprint> findByProjectId(@Param("projectId") Long projectId);

}
