package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.PRDetailedAnalysisDTO;
import org.trackdev.api.dto.PRFileDetailDTO;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.mapper.UserMapper;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.PullRequestService;

import java.security.Principal;
import java.util.List;

/**
 * Controller for Pull Request analysis endpoints
 */
@RestController
@RequestMapping(path = "/pull-requests")
@Tag(name = "Pull Requests", description = "Pull Request analysis endpoints")
public class PullRequestController extends BaseController {

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AccessChecker accessChecker;

    @Operation(summary = "Get detailed PR analysis with file information",
            description = "Returns the pull request details including file-level surviving lines information",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/{prId}/details")
    public PRDetailedAnalysisDTO getPRDetails(@PathVariable(name = "prId") String prId, Principal principal) {
        String userId = getUserId(principal);
        
        PullRequest pr = pullRequestService.get(prId);
        accessChecker.checkCanViewPullRequest(pr, userId);
        
        // Get file details
        List<PRFileDetailDTO> fileDetails = pullRequestService.getFileDetails(prId);
        
        // Build response DTO
        PRDetailedAnalysisDTO response = new PRDetailedAnalysisDTO();
        response.setId(pr.getId());
        response.setUrl(pr.getUrl());
        response.setPrNumber(pr.getPrNumber());
        response.setTitle(pr.getTitle());
        response.setState(pr.getState());
        response.setMerged(pr.getMerged());
        response.setRepoFullName(pr.getRepoFullName());
        response.setAdditions(pr.getAdditions());
        response.setDeletions(pr.getDeletions());
        response.setChangedFiles(pr.getChangedFiles());
        response.setFiles(fileDetails);
        
        // Calculate total surviving lines
        int totalSurviving = fileDetails.stream()
                .mapToInt(f -> f.getSurvivingLines() != null ? f.getSurvivingLines() : 0)
                .sum();
        response.setSurvivingLines(totalSurviving);
        
        if (pr.getAuthor() != null) {
            response.setAuthor(userMapper.toSummaryDTO(pr.getAuthor()));
        }
        
        return response;
    }

    @Operation(summary = "Get file details for a PR",
            description = "Returns detailed file information with line ranges for a pull request",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/{prId}/files")
    public List<PRFileDetailDTO> getPRFiles(@PathVariable(name = "prId") String prId, Principal principal) {
        String userId = getUserId(principal);
        
        PullRequest pr = pullRequestService.get(prId);
        accessChecker.checkCanViewPullRequest(pr, userId);
        
        return pullRequestService.getFileDetails(prId);
    }
}
