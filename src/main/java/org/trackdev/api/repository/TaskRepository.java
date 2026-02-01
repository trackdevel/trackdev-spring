package org.trackdev.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends BaseRepositoryLong<Task> {
    
    /**
     * Find a task by its unique task key (e.g., "a7k-1", "prj-42")
     */
    Optional<Task> findByTaskKey(String taskKey);

    /**
     * Find all tasks where the user is reporter or assignee, ordered by creation date desc
     */
    List<Task> findByReporterOrAssigneeOrderByCreatedAtDesc(User reporter, User assignee);

    /**
     * Find all tasks where the user is reporter or assignee, paginated
     */
    Page<Task> findByReporterOrAssigneeOrderByCreatedAtDesc(User reporter, User assignee, Pageable pageable);

    /**
     * Find latest N tasks where the user is reporter or assignee
     */
    List<Task> findTop5ByReporterOrAssigneeOrderByCreatedAtDesc(User reporter, User assignee);

    /**
     * Find all tasks in the given projects, ordered by creation date desc
     */
    List<Task> findByProjectInOrderByCreatedAtDesc(Collection<Project> projects);

    /**
     * Find all tasks in the given projects, paginated
     */
    Page<Task> findByProjectInOrderByCreatedAtDesc(Collection<Project> projects, Pageable pageable);

    /**
     * Find latest N tasks in the given projects
     */
    List<Task> findTop5ByProjectInOrderByCreatedAtDesc(Collection<Project> projects);

    /**
     * Check if a project has any tasks
     */
    boolean existsByProjectId(Long projectId);

    /**
     * Find all tasks in a project with a specific status
     */
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    /**
     * Find all tasks in a project with a specific status and assignee
     */
    List<Task> findByProjectIdAndStatusAndAssigneeId(Long projectId, TaskStatus status, String assigneeId);
}
