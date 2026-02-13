package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.SprintPatternDTO;
import org.trackdev.api.dto.SprintPatternsResponseDTO;
import org.trackdev.api.entity.SprintPattern;
import org.trackdev.api.mapper.SprintPatternMapper;
import org.trackdev.api.model.SprintPatternRequest;
import org.trackdev.api.service.SprintPatternService;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * Controller for sprint pattern management.
 * Sprint patterns are templates that can be applied to projects.
 * Only professors can create, edit, and delete sprint patterns.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "8. Sprint Patterns")
@RestController
@RequestMapping(path = "/sprint-patterns")
public class SprintPatternController extends BaseController {

    @Autowired
    SprintPatternService service;

    @Autowired
    SprintPatternMapper mapper;

    @Operation(summary = "Get all sprint patterns for a course", description = "Get all sprint patterns belonging to a course")
    @GetMapping(path = "/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public SprintPatternsResponseDTO getPatternsByCourse(Principal principal, @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        List<SprintPattern> patterns = service.getPatternsByCourse(courseId, userId);
        return new SprintPatternsResponseDTO(mapper.toDTOList(patterns));
    }

    @Operation(summary = "Get a specific sprint pattern", description = "Get a sprint pattern by ID")
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public SprintPatternDTO getPattern(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        SprintPattern pattern = service.getPattern(id, userId);
        return mapper.toDTO(pattern);
    }

    @Operation(summary = "Create a new sprint pattern", description = "Create a new sprint pattern for a course (professors only)")
    @PostMapping(path = "/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public SprintPatternDTO createPattern(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @Valid @RequestBody SprintPatternRequest request) {
        String userId = super.getUserId(principal);
        SprintPattern pattern = service.createPattern(courseId, request, userId);
        return mapper.toDTO(pattern);
    }

    @Operation(summary = "Update a sprint pattern", description = "Update an existing sprint pattern (professors only)")
    @PutMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public SprintPatternDTO updatePattern(
            Principal principal,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody SprintPatternRequest request) {
        String userId = super.getUserId(principal);
        SprintPattern pattern = service.updatePattern(id, request, userId);
        return mapper.toDTO(pattern);
    }

    @Operation(summary = "Delete a sprint pattern", description = "Delete a sprint pattern (professors only)")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deletePattern(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deletePattern(id, userId);
        return ResponseEntity.noContent().build();
    }
}
