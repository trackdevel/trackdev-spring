package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.IdResponseDTO;
import org.trackdev.api.dto.WorkspaceBasicDTO;
import org.trackdev.api.dto.WorkspaceCompleteDTO;
import org.trackdev.api.entity.Workspace;
import org.trackdev.api.mapper.WorkspaceMapper;
import org.trackdev.api.model.CreateWorkspaceRequest;
import org.trackdev.api.model.MergePatchWorkspace;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.WorkspaceService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * Controller for workspace management.
 * Workspaces provide multi-tenancy support for the application.
 * Only ADMIN users can create and view all workspaces.
 * WORKSPACE_ADMIN can manage their own workspace.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "1. Workspaces")
@RestController
@RequestMapping(path = "/workspaces")
public class WorkspaceController extends CrudController<Workspace, WorkspaceService> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    WorkspaceMapper workspaceMapper;

    @Operation(summary = "Get all workspaces", description = "Get all workspaces - admin only")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<WorkspaceBasicDTO> getAllWorkspaces(Principal principal) {
        String userId = super.getUserId(principal);
        return workspaceMapper.toBasicDTOList(service.getAllWorkspaces(userId));
    }

    @Operation(summary = "Get specific workspace", description = "Get specific workspace details")
    @GetMapping(path = "/{id}")
    public WorkspaceCompleteDTO getWorkspace(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        return workspaceMapper.toCompleteDTO(service.getWorkspace(id, userId));
    }

    @Operation(summary = "Create workspace", description = "Create a new workspace - admin only")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public IdResponseDTO createWorkspace(Principal principal, @Valid @RequestBody CreateWorkspaceRequest request) {
        String userId = super.getUserId(principal);
        
        if (request.getName() == null || request.getName().isEmpty() || 
            request.getName().length() > Workspace.NAME_LENGTH) {
            throw new ControllerException(ErrorConstants.INVALID_WORKSPACE_NAME_LENGTH);
        }
        
        Workspace workspace = service.createWorkspace(request.getName(), userId);
        return new IdResponseDTO(workspace.getId());
    }

    @Operation(summary = "Edit workspace", description = "Edit workspace details")
    @PatchMapping(path = "/{id}")
    public WorkspaceBasicDTO editWorkspace(Principal principal, 
                                            @PathVariable(name = "id") Long id,
                                            @Valid @RequestBody MergePatchWorkspace request) {
        String userId = super.getUserId(principal);
        
        String name = null;
        if (request.name != null) {
            name = request.name.get();
            if (name.isEmpty() || name.length() > Workspace.NAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_WORKSPACE_NAME_LENGTH);
            }
        }
        
        return workspaceMapper.toBasicDTO(service.editWorkspace(id, name, userId));
    }

    @Operation(summary = "Delete workspace", description = "Delete a workspace - admin only")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspace(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteWorkspace(id, userId);
    }
}
