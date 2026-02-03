package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.PRFileDetailDTO;
import org.trackdev.api.dto.ProjectAnalysisDTO;
import org.trackdev.api.entity.User;
import org.trackdev.api.service.ProjectAnalysisService;
import org.trackdev.api.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/project-analyses")
@Tag(name = "Project Analysis", description = "Full project analysis endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectAnalysisController extends BaseController {

    @Autowired
    private ProjectAnalysisService analysisService;

    @Autowired
    private UserService userService;

    /**
     * Start a new project analysis.
     * Only professors/admins who can manage the course can start an analysis.
     */
    @PostMapping("/projects/{projectId}/start")
    @Operation(summary = "Start a new project analysis", 
               description = "Starts an asynchronous analysis of all DONE tasks' PRs in the project")
    public ResponseEntity<ProjectAnalysisDTO> startAnalysis(
            Principal principal,
            @PathVariable(name = "projectId") Long projectId) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        ProjectAnalysisDTO result = analysisService.startAnalysis(projectId, currentUser);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    /**
     * Get the status of a specific analysis
     */
    @GetMapping("/{analysisId}")
    @Operation(summary = "Get analysis status", description = "Get the current status and progress of an analysis")
    public ResponseEntity<ProjectAnalysisDTO> getAnalysisStatus(
            Principal principal,
            @PathVariable(name = "analysisId") String analysisId) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        ProjectAnalysisDTO result = analysisService.getAnalysisStatus(analysisId, currentUser);
        return ResponseEntity.ok(result);
    }

    /**
     * Get the latest analysis for a project
     */
    @GetMapping("/projects/{projectId}/latest")
    @Operation(summary = "Get latest analysis", description = "Get the most recent analysis for a project")
    public ResponseEntity<ProjectAnalysisDTO> getLatestAnalysis(
            Principal principal,
            @PathVariable(name = "projectId") Long projectId) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        ProjectAnalysisDTO result = analysisService.getLatestAnalysis(projectId, currentUser);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get all analyses for a project
     */
    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get all analyses", description = "Get all analyses for a project")
    public ResponseEntity<List<ProjectAnalysisDTO>> getProjectAnalyses(
            Principal principal,
            @PathVariable(name = "projectId") Long projectId) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        List<ProjectAnalysisDTO> results = analysisService.getProjectAnalyses(projectId, currentUser);
        return ResponseEntity.ok(results);
    }

    /**
     * Get analysis results with optional filters
     */
    @GetMapping("/{analysisId}/results")
    @Operation(summary = "Get analysis results", 
               description = "Get detailed results with optional sprint and author filters")
    public ResponseEntity<ProjectAnalysisDTO.ResultsDTO> getAnalysisResults(
            Principal principal,
            @PathVariable(name = "analysisId") String analysisId,
            @RequestParam(name = "sprintId", required = false) Long sprintId,
            @RequestParam(name = "authorId", required = false) String authorId) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        ProjectAnalysisDTO.ResultsDTO results = analysisService.getAnalysisResults(
                analysisId, sprintId, authorId, currentUser);
        return ResponseEntity.ok(results);
    }

    /**
     * Get precomputed file details for a specific PR in an analysis.
     * Returns the same format as the PR analysis endpoint but from precomputed data.
     */
    @GetMapping("/{analysisId}/prs/{prId}/files")
    @Operation(summary = "Get precomputed PR file details", 
               description = "Get precomputed per-line analysis for a PR from a project analysis")
    public ResponseEntity<List<PRFileDetailDTO>> getPrecomputedFileDetails(
            Principal principal,
            @PathVariable(name = "analysisId") String analysisId,
            @PathVariable(name = "prId") String prId) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        List<PRFileDetailDTO> result = analysisService.getPrecomputedFileDetails(analysisId, prId, currentUser);
        return ResponseEntity.ok(result);
    }
}
