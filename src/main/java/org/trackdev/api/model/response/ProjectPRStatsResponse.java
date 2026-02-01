package org.trackdev.api.model.response;

import org.trackdev.api.dto.PullRequestDTO;

import java.util.Collection;
import java.util.List;

/**
 * Response containing PR statistics for a project's completed tasks
 */
public record ProjectPRStatsResponse(
    Long projectId,
    List<TaskWithPRStats> tasks
) {
    /**
     * Task with its associated pull requests and stats
     */
    public record TaskWithPRStats(
        Long taskId,
        String taskKey,
        String taskName,
        String assigneeFullName,
        String assigneeUsername,
        Collection<PullRequestDTO> pullRequests
    ) {}
}
