package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.entity.GitHubRepo;
import org.trackdev.api.service.GitHubRepoService;

import java.security.Principal;
import java.util.List;

/**
 * Global search endpoint for GitHub repositories (not project-scoped).
 * Only accessible by PROFESSOR or ADMIN.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "9. GitHub Repositories")
@RestController
@RequestMapping(path = "/github-repos")
public class GitHubRepoSearchController extends BaseController {

    @Autowired
    private GitHubRepoService gitHubRepoService;

    @Operation(summary = "Search GitHub repositories by full name",
               description = "Finds all GitHub repositories matching the given owner/repo name. Only accessible by PROFESSOR or ADMIN.")
    @GetMapping
    public GitHubRepoSearchResponse searchByFullName(Principal principal,
                                                     @RequestParam @NotBlank String fullName) {
        String userId = getUserId(principal);
        List<GitHubRepo> repos = gitHubRepoService.findByFullName(fullName, userId);
        return new GitHubRepoSearchResponse(repos);
    }

    static class GitHubRepoSearchResponse {
        public List<GitHubRepoResult> repos;
        public int count;

        public GitHubRepoSearchResponse(List<GitHubRepo> repos) {
            this.repos = repos.stream().map(GitHubRepoResult::new).toList();
            this.count = repos.size();
        }
    }

    static class GitHubRepoResult {
        public Long id;
        public String name;
        public String fullName;
        public Long projectId;
        public boolean webhookActive;

        public GitHubRepoResult(GitHubRepo repo) {
            this.id = repo.getId();
            this.name = repo.getName();
            this.fullName = repo.getFullName();
            this.projectId = repo.getProjectId();
            this.webhookActive = repo.isWebhookActive();
        }
    }
}
