package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.entity.GitHubRepo;
import org.trackdev.api.service.GitHubRepoService;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing GitHub repositories linked to projects.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "9. GitHub Repositories")
@RestController
@RequestMapping(path = "/projects/{projectId}/github-repos")
public class GitHubRepoController extends BaseController {

    @Autowired
    private GitHubRepoService gitHubRepoService;

    @Operation(summary = "Get all GitHub repositories for a project", 
               description = "Returns all GitHub repositories linked to the specified project")
    @GetMapping
    public GitHubReposResponse getProjectRepos(Principal principal, @PathVariable(name = "projectId") Long projectId) {
        String userId = getUserId(principal);
        Collection<GitHubRepo> repos = gitHubRepoService.getProjectRepos(projectId, userId);
        return new GitHubReposResponse(repos, projectId);
    }

    @Operation(summary = "Get a specific GitHub repository", 
               description = "Returns details of a specific GitHub repository")
    @GetMapping("/{repoId}")
    public GitHubRepoResponse getRepo(Principal principal, 
                                      @PathVariable(name = "projectId") Long projectId, 
                                      @PathVariable(name = "repoId") Long repoId) {
        String userId = getUserId(principal);
        GitHubRepo repo = gitHubRepoService.getRepo(projectId, repoId, userId);
        return new GitHubRepoResponse(repo);
    }

    @Operation(summary = "Add a GitHub repository to a project", 
               description = "Links a new GitHub repository to the project with the provided access token")
    @PostMapping
    public GitHubRepoResponse addRepository(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId,
                                            @Valid @RequestBody AddRepoRequest request) {
        String userId = getUserId(principal);
        GitHubRepo repo = gitHubRepoService.addRepository(
                projectId, request.name, request.url, request.accessToken, userId);
        return new GitHubRepoResponse(repo);
    }

    @Operation(summary = "Update a GitHub repository", 
               description = "Updates the name or access token of a GitHub repository")
    @PatchMapping("/{repoId}")
    public GitHubRepoResponse updateRepository(Principal principal,
                                               @PathVariable(name = "projectId") Long projectId,
                                               @PathVariable(name = "repoId") Long repoId,
                                               @Valid @RequestBody UpdateRepoRequest request) {
        String userId = getUserId(principal);
        GitHubRepo repo = gitHubRepoService.updateRepository(
                projectId, repoId, request.name, request.accessToken, userId);
        return new GitHubRepoResponse(repo);
    }

    @Operation(summary = "Delete a GitHub repository from a project", 
               description = "Removes the GitHub repository link and deletes any associated webhooks")
    @DeleteMapping("/{repoId}")
    public ResponseEntity<Void> deleteRepository(Principal principal,
                                                 @PathVariable(name = "projectId") Long projectId,
                                                 @PathVariable(name = "repoId") Long repoId) {
        String userId = getUserId(principal);
        gitHubRepoService.deleteRepository(projectId, repoId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a webhook for the repository", 
               description = "Creates a webhook on the GitHub repository to receive events")
    @PostMapping("/{repoId}/webhook")
    public GitHubRepoResponse createWebhook(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId,
                                            @PathVariable(name = "repoId") Long repoId,
                                            @Valid @RequestBody CreateWebhookRequest request) {
        String userId = getUserId(principal);
        GitHubRepo repo = gitHubRepoService.createWebhook(projectId, repoId, request.webhookUrl, userId);
        return new GitHubRepoResponse(repo);
    }

    @Operation(summary = "Delete the webhook from the repository", 
               description = "Removes the webhook from the GitHub repository")
    @DeleteMapping("/{repoId}/webhook")
    public GitHubRepoResponse deleteWebhook(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId,
                                            @PathVariable(name = "repoId") Long repoId) {
        String userId = getUserId(principal);
        GitHubRepo repo = gitHubRepoService.deleteWebhook(projectId, repoId, userId);
        return new GitHubRepoResponse(repo);
    }

    @Operation(summary = "Get repository information from GitHub", 
               description = "Fetches current repository information from the GitHub API")
    @GetMapping("/{repoId}/info")
    public Map<String, Object> getRepositoryInfo(Principal principal,
                                                 @PathVariable(name = "projectId") Long projectId,
                                                 @PathVariable(name = "repoId") Long repoId) {
        String userId = getUserId(principal);
        return gitHubRepoService.getRepositoryInfo(projectId, repoId, userId);
    }

    @Operation(summary = "Get recent commits from the repository", 
               description = "Fetches recent commits from the GitHub repository")
    @GetMapping("/{repoId}/commits")
    public List<Map<String, Object>> getCommits(Principal principal,
                                                @PathVariable(name = "projectId") Long projectId,
                                                @PathVariable(name = "repoId") Long repoId,
                                                @RequestParam(name = "limit", defaultValue = "30") int limit) {
        String userId = getUserId(principal);
        return gitHubRepoService.getCommits(projectId, repoId, userId, limit);
    }

    @Operation(summary = "Get branches from the repository", 
               description = "Fetches all branches from the GitHub repository")
    @GetMapping("/{repoId}/branches")
    public List<Map<String, Object>> getBranches(Principal principal,
                                                 @PathVariable(name = "projectId") Long projectId,
                                                 @PathVariable(name = "repoId") Long repoId) {
        String userId = getUserId(principal);
        return gitHubRepoService.getBranches(projectId, repoId, userId);
    }

    // ========== REQUEST/RESPONSE DTOs ==========

    static class AddRepoRequest {
        @NotBlank
        @Size(min = 1, max = GitHubRepo.MAX_NAME_LENGTH)
        public String name;

        @NotBlank
        @Size(min = 1, max = GitHubRepo.MAX_URL_LENGTH)
        public String url;

        @NotBlank
        @Size(min = 1, max = GitHubRepo.MAX_TOKEN_LENGTH)
        public String accessToken;
    }

    static class UpdateRepoRequest {
        @Size(min = 1, max = GitHubRepo.MAX_NAME_LENGTH)
        public String name;

        @Size(min = 1, max = GitHubRepo.MAX_TOKEN_LENGTH)
        public String accessToken;
    }

    static class CreateWebhookRequest {
        @NotBlank
        @Size(min = 1, max = GitHubRepo.MAX_URL_LENGTH)
        public String webhookUrl;
    }

    static class GitHubRepoResponse {
        public Long id;
        public String name;
        public String url;
        public String owner;
        public String repoName;
        public String fullName;
        public Long projectId;
        public String createdAt;
        public String lastSyncAt;
        public boolean webhookActive;
        public String webhookUrl;

        public GitHubRepoResponse(GitHubRepo repo) {
            this.id = repo.getId();
            this.name = repo.getName();
            this.url = repo.getUrl();
            this.owner = repo.getOwner();
            this.repoName = repo.getRepoName();
            this.fullName = repo.getFullName();
            this.projectId = repo.getProjectId();
            this.createdAt = repo.getCreatedAt() != null ? repo.getCreatedAt().toString() : null;
            this.lastSyncAt = repo.getLastSyncAt() != null ? repo.getLastSyncAt().toString() : null;
            this.webhookActive = repo.isWebhookActive();
            this.webhookUrl = repo.getWebhookUrl();
        }
    }

    static class GitHubReposResponse {
        public Collection<GitHubRepoSummary> repos;
        public Long projectId;
        public int count;

        public GitHubReposResponse(Collection<GitHubRepo> repos, Long projectId) {
            this.repos = repos.stream().map(GitHubRepoSummary::new).toList();
            this.projectId = projectId;
            this.count = repos.size();
        }
    }

    static class GitHubRepoSummary {
        public Long id;
        public String name;
        public String fullName;
        public boolean webhookActive;

        public GitHubRepoSummary(GitHubRepo repo) {
            this.id = repo.getId();
            this.name = repo.getName();
            this.fullName = repo.getFullName();
            this.webhookActive = repo.isWebhookActive();
        }
    }
}
