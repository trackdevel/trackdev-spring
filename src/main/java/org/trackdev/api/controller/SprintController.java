package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.HistoryResponseDTO;
import org.trackdev.api.dto.SprintBasicDTO;
import org.trackdev.api.dto.SprintBoardDTO;
import org.trackdev.api.dto.SprintsResponseDTO;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.sprintchanges.SprintChange;
import org.trackdev.api.mapper.SprintMapper;
import org.trackdev.api.mapper.TaskMapper;
import org.trackdev.api.model.MergePatchSprint;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.SprintChangeService;
import org.trackdev.api.service.SprintService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "7. Sprints")
@RestController
@RequestMapping(path = "/sprints")
public class SprintController extends CrudController<Sprint, SprintService> {
    @Autowired
    AccessChecker accessChecker;

    @Autowired
    SprintChangeService sprintChangeService;

    @Autowired
    SprintMapper sprintMapper;

    @Autowired
    TaskMapper taskMapper;

    @Operation(summary = "Get all sprints", description = "Get all sprints")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SprintsResponseDTO getSprints(Principal principal) {
        String userId = super.getUserId(principal);
        return new SprintsResponseDTO(sprintMapper.toBasicDTOList(service.findAll()));
    }

    @Operation(summary = "Get specific sprint", description = "Get specific sprint")
    @GetMapping(path = "/{id}")
    public SprintBasicDTO getSprint(Principal principal, @PathVariable Long id) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        return sprintMapper.toBasicDTO(service.getSprint(id, userId));
    }

    @Operation(summary = "Get sprint board with tasks", description = "Get sprint with all tasks for board view")
    @GetMapping(path = "/{id}/board")
    public SprintBoardDTO getSprintBoard(Principal principal, @PathVariable Long id) {
        String userId = super.getUserId(principal);
        Sprint sprint = service.getSprint(id, userId);
        SprintBoardDTO boardDTO = sprintMapper.toBoardDTO(sprint);
        // Manually set tasks to avoid circular dependency between TaskMapper and SprintMapper
        boardDTO.setTasks(taskMapper.toBasicDTOCollection(sprint.getActiveTasks()));
        return boardDTO;
    }

    @Operation(summary = "Edit specific sprint", description = "Edit specific sprint")
    @PatchMapping(path = "/{id}")
    public SprintBasicDTO editSprint(Principal principal,
                         @PathVariable Long id,
                         @Valid @RequestBody MergePatchSprint sprintRequest) {
        if (sprintRequest.name != null){
            if (sprintRequest.name.get().isEmpty() || sprintRequest.name.get().length() > Sprint.NAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_SPRINT_NAME_LENGTH);
            }
        }
        String userId = super.getUserId(principal);
        return sprintMapper.toBasicDTO(service.editSprint(id, sprintRequest, userId));
    }

    @Operation(summary = "Get history of logs of the sprint", description = "Get history of logs of the sprint")
    @GetMapping(path = "/{id}/history")
    public HistoryResponseDTO<SprintChange> getHistory(Principal principal, @PathVariable Long id,
                                         @RequestParam(required = false) String search) {
        String userId = super.getUserId(principal);
        // Auth check and data retrieval in a single transaction
        List<SprintChange> history = service.getSprintHistory(id, userId, search);
        return new HistoryResponseDTO<>(history, id);
    }

    @Operation(summary = "Delete specific sprint", description = "Delete specific sprint - only course owner (professor) or admin")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteSprint(Principal principal, @PathVariable Long id) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method (within transaction)
        service.deleteSprint(id, userId);
        return okNoContent();
    }

}
