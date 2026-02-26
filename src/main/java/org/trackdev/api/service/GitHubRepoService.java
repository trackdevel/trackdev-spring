package org.trackdev.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.GitHubRepo;
import org.trackdev.api.entity.Project;
import org.trackdev.api.repository.GitHubRepoRepository;
import org.trackdev.api.utils.ErrorConstants;
import org.trackdev.api.utils.GithubConstants;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for managing GitHub repositories linked to projects.
 * Handles repository CRUD operations and webhook management.
 */
@Service
public class GitHubRepoService extends BaseServiceLong<GitHubRepo, GitHubRepoRepository> {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AccessChecker accessChecker;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubRepoService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get all GitHub repositories for a project.
     */
    public Collection<GitHubRepo> getProjectRepos(Long projectId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return repo.findByProjectId(projectId);
    }

    /**
     * Get a specific GitHub repository.
     */
    public GitHubRepo getRepo(Long projectId, Long repoId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));
    }

    /**
     * Add a GitHub repository to a project.
     */
    @Transactional
    public GitHubRepo addRepository(Long projectId, String name, String url, String accessToken, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageGitHubRepos(project, userId);

        // Validate URL format
        if (!isValidGitHubUrl(url)) {
            throw new ServiceException(ErrorConstants.INVALID_GITHUB_URL);
        }

        // Check if repository already exists for this project
        if (repo.existsByProjectIdAndUrl(projectId, url)) {
            throw new ServiceException(ErrorConstants.GITHUB_REPO_ALREADY_EXISTS);
        }

        // Validate token by checking repository access
        GitHubRepo gitHubRepo = new GitHubRepo(name, url, accessToken, project);
        validateRepositoryAccess(gitHubRepo);

        project.addGithubRepo(gitHubRepo);
        repo.save(gitHubRepo);

        return gitHubRepo;
    }

    /**
     * Update a GitHub repository.
     */
    @Transactional
    public GitHubRepo updateRepository(Long projectId, Long repoId, String name, String accessToken, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageGitHubRepos(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        if (name != null && !name.isEmpty()) {
            gitHubRepo.setName(name);
        }

        if (accessToken != null && !accessToken.isEmpty()) {
            gitHubRepo.setAccessToken(accessToken);
            validateRepositoryAccess(gitHubRepo);
        }

        repo.save(gitHubRepo);
        return gitHubRepo;
    }

    /**
     * Delete a GitHub repository from a project.
     */
    @Transactional
    public void deleteRepository(Long projectId, Long repoId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageGitHubRepos(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        project.removeGithubRepo(gitHubRepo);
        repo.delete(gitHubRepo);
    }

    /**
     * Get repository information from GitHub API.
     */
    public Map<String, Object> getRepositoryInfo(Long projectId, Long repoId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        return fetchRepositoryInfo(gitHubRepo);
    }

    /**
     * Get commits from a GitHub repository.
     */
    public List<Map<String, Object>> getCommits(Long projectId, Long repoId, String userId, int limit) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        return fetchCommits(gitHubRepo, limit);
    }

    /**
     * Get branches from a GitHub repository.
     */
    public List<Map<String, Object>> getBranches(Long projectId, Long repoId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        return fetchBranches(gitHubRepo);
    }

    /**
     * Find a GitHub repository by its URL.
     */
    public Optional<GitHubRepo> findByUrl(String url) {
        return repo.findByUrl(url);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    private boolean isValidGitHubUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.contains("github.com/") || url.contains("git@github.com:");
    }

    private void validateRepositoryAccess(GitHubRepo gitHubRepo) {
        String owner = gitHubRepo.getOwner();
        String repoName = gitHubRepo.getRepoName();

        if (owner == null || repoName == null) {
            throw new ServiceException(ErrorConstants.INVALID_GITHUB_URL);
        }

        try {
            HttpHeaders headers = createAuthHeaders(gitHubRepo.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(headers);
            String repoUrl = GithubConstants.getRepoUrl(owner, repoName);

            ResponseEntity<String> response = restTemplate.exchange(
                    repoUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND);
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ServiceException(ErrorConstants.GITHUB_TOKEN_INVALID, e);
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new ServiceException(ErrorConstants.GITHUB_REPO_ACCESS_DENIED, e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND, e);
            }
            throw new ServiceException(ErrorConstants.API_GITHUB_KO, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchRepositoryInfo(GitHubRepo gitHubRepo) {
        try {
            HttpHeaders headers = createAuthHeaders(gitHubRepo.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(headers);
            String repoUrl = GithubConstants.getRepoUrl(gitHubRepo.getOwner(), gitHubRepo.getRepoName());

            ResponseEntity<String> response = restTemplate.exchange(
                    repoUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            gitHubRepo.setLastSyncAt(ZonedDateTime.now(ZoneId.of("UTC")));
            repo.save(gitHubRepo);

            return objectMapper.readValue(response.getBody(), Map.class);

        } catch (Exception e) {
            throw new ServiceException(ErrorConstants.API_GITHUB_KO, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchCommits(GitHubRepo gitHubRepo, int limit) {
        try {
            HttpHeaders headers = createAuthHeaders(gitHubRepo.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(headers);
            String commitsUrl = GithubConstants.getCommitsUrl(gitHubRepo.getOwner(), gitHubRepo.getRepoName()) 
                    + "?per_page=" + Math.min(limit, 100);

            ResponseEntity<String> response = restTemplate.exchange(
                    commitsUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return objectMapper.readValue(response.getBody(), List.class);

        } catch (Exception e) {
            throw new ServiceException(ErrorConstants.API_GITHUB_KO, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchBranches(GitHubRepo gitHubRepo) {
        try {
            HttpHeaders headers = createAuthHeaders(gitHubRepo.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(headers);
            String branchesUrl = GithubConstants.getBranchesUrl(gitHubRepo.getOwner(), gitHubRepo.getRepoName());

            ResponseEntity<String> response = restTemplate.exchange(
                    branchesUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return objectMapper.readValue(response.getBody(), List.class);

        } catch (Exception e) {
            throw new ServiceException(ErrorConstants.API_GITHUB_KO, e);
        }
    }
}
