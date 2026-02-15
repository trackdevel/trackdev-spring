package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.CommentDTO;
import org.trackdev.api.dto.TaskBasicDTO;
import org.trackdev.api.dto.TaskCompleteDTO;
import org.trackdev.api.dto.TaskDetailDTO;
import org.trackdev.api.dto.TaskWithProjectDTO;
import org.trackdev.api.entity.Comment;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskStatus;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SprintMapper.class, CommentMapper.class, ProjectMapper.class, PullRequestMapper.class})
public interface TaskMapper {

    @Named("taskToBasicDTO")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "activeSprints", source = "activeSprints", qualifiedByName = "sprintToBasicDTO")
    @Mapping(target = "pullRequests", source = "pullRequests", qualifiedByName = "pullRequestToDTO")
    @Mapping(target = "parentTaskId", source = "parentTaskId")
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "childTasks", source = "childTasks", qualifiedByName = "childTasksToBasicDTO")
    TaskBasicDTO toBasicDTO(Task task);

    @Named("taskToCompleteDTO")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "activeSprints", source = "activeSprints", qualifiedByName = "sprintToBasicDTO")
    @Mapping(target = "pullRequests", source = "pullRequests", qualifiedByName = "pullRequestToDTO")
    @Mapping(target = "discussion", source = "discussion", qualifiedByName = "discussionToDTO")
    @Mapping(target = "parentTaskId", source = "parentTaskId")
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "childTasks", ignore = true)
    TaskCompleteDTO toCompleteDTO(Task task);

    @Named("taskToWithProjectDTO")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "activeSprints", source = "activeSprints", qualifiedByName = "sprintToBasicDTO")
    @Mapping(target = "pullRequests", source = "pullRequests", qualifiedByName = "pullRequestToDTO")
    @Mapping(target = "discussion", source = "discussion", qualifiedByName = "discussionToDTO")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectToWithMembersDTO")
    @Mapping(target = "parentTaskId", source = "parentTaskId")
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "childTasks", ignore = true)
    TaskWithProjectDTO toWithProjectDTO(Task task);

    @Named("taskToDetailDTO")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "activeSprints", source = "activeSprints", qualifiedByName = "sprintToBasicDTO")
    @Mapping(target = "pullRequests", source = "pullRequests", qualifiedByName = "pullRequestToDTO")
    @Mapping(target = "discussion", source = "discussion", qualifiedByName = "discussionToDTO")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectToWithMembersDTO")
    @Mapping(target = "parentTaskId", source = "parentTaskId")
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "childTasks", source = "childTasks", qualifiedByName = "childTasksToBasicDTO")
    @Mapping(target = "pointsReview", ignore = true)
    // Permission flags - computed in controller based on user context
    @Mapping(target = "canEdit", ignore = true)
    @Mapping(target = "canEditStatus", ignore = true)
    @Mapping(target = "canEditSprint", ignore = true)
    @Mapping(target = "canEditType", ignore = true)
    @Mapping(target = "canEditEstimation", ignore = true)
    @Mapping(target = "canDelete", ignore = true)
    @Mapping(target = "canSelfAssign", ignore = true)
    @Mapping(target = "canUnassign", ignore = true)
    @Mapping(target = "canAddSubtask", ignore = true)
    @Mapping(target = "canFreeze", ignore = true)
    @Mapping(target = "canComment", ignore = true)
    // Points review flags - computed in controller
    @Mapping(target = "canStartPointsReview", ignore = true)
    @Mapping(target = "canViewPointsReviews", ignore = true)
    @Mapping(target = "pointsReviewConversationCount", ignore = true)
    TaskDetailDTO toDetailDTO(Task task);

    /**
     * Maps a task to a shallow DTO (no nested childTasks) to avoid infinite recursion
     */
    @Named("taskToShallowBasicDTO")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "activeSprints", source = "activeSprints", qualifiedByName = "sprintToBasicDTO")
    @Mapping(target = "pullRequests", source = "pullRequests", qualifiedByName = "pullRequestToDTO")
    @Mapping(target = "parentTaskId", source = "parentTaskId")
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "childTasks", ignore = true)
    TaskBasicDTO toShallowBasicDTO(Task task);

    @Named("childTasksToBasicDTO")
    @IterableMapping(qualifiedByName = "taskToShallowBasicDTO")
    Collection<TaskBasicDTO> childTasksToBasicDTO(Collection<Task> tasks);

    @IterableMapping(qualifiedByName = "taskToBasicDTO")
    List<TaskBasicDTO> toBasicDTOList(List<Task> tasks);

    @IterableMapping(qualifiedByName = "taskToBasicDTO")
    Collection<TaskBasicDTO> toBasicDTOCollection(Collection<Task> tasks);

    @Named("discussionToDTO")
    @IterableMapping(qualifiedByName = "commentToDTO")
    Collection<CommentDTO> discussionToDTO(Collection<Comment> comments);

    @Named("taskStatusToString")
    default String statusToString(TaskStatus status) {
        return status != null ? status.name() : null;
    }
}
