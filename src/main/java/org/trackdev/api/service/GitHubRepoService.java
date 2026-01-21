package org.trackdev.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger log = LoggerFactory.getLogger(GitHubRepoService.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AccessChecker accessChecker;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${trackdev.webhook.url:}")
    private String webhookBaseUrl;

    @Value("${trackdev.webhook.secret:}")
    private String webhookSecret;

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
     * Automatically creates a webhook for PR events if webhookBaseUrl is configured.
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

        // Auto-create webhook for PR events if webhookBaseUrl is configured
        if (webhookBaseUrl != null && !webhookBaseUrl.isEmpty()) {
            try {
                String webhookUrl = webhookBaseUrl + "/api/hooks/github/pr";
                log.info("Creating webhook for repository {} -> {}", gitHubRepo.getFullName(), webhookUrl);
                createWebhookInternal(gitHubRepo, webhookUrl);
                log.info("Webhook created successfully for repository {}", gitHubRepo.getFullName());
            } catch (Exception e) {
                // Log but don't fail - webhook creation is optional
                // The repository is still added successfully
                log.warn("Failed to create webhook for repository {}: {}", gitHubRepo.getFullName(), e.getMessage());
            }
        } else {
            log.info("Skipping webhook creation - webhookBaseUrl not configured (set WEBHOOK_BASE_URL environment variable)");
        }

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

        // Remove webhook if active
        if (gitHubRepo.isWebhookActive() && gitHubRepo.getWebhookId() != null) {
            try {
                deleteWebhook(gitHubRepo);
            } catch (Exception e) {
                // Log but don't fail - webhook might already be deleted on GitHub
            }
        }

        project.removeGithubRepo(gitHubRepo);
        repo.delete(gitHubRepo);
    }

    /**
     * Create a webhook for a GitHub repository.
     */
    @Transactional
    public GitHubRepo createWebhook(Long projectId, Long repoId, String webhookUrl, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageGitHubRepos(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        if (gitHubRepo.isWebhookActive()) {
            // Webhook already exists, return current state
            return gitHubRepo;
        }

        createWebhookInternal(gitHubRepo, webhookUrl);
        return gitHubRepo;
    }

    /**
     * Internal method to create a webhook on a GitHub repository.
     * Generates a unique secret per repository for signature verification.
     */
    private void createWebhookInternal(GitHubRepo gitHubRepo, String webhookUrl) {
        String owner = gitHubRepo.getOwner();
        String repoName = gitHubRepo.getRepoName();

        if (owner == null || repoName == null) {
            throw new ServiceException(ErrorConstants.INVALID_GITHUB_URL);
        }

        // Generate a unique secret for this repository
        String repoWebhookSecret = generateWebhookSecret();
        
        try {
            HttpHeaders headers = createAuthHeaders(gitHubRepo.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build webhook payload
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("name", "web");
            webhookPayload.put("active", true);
            
            Map<String, Object> config = new HashMap<>();
            config.put("url", webhookUrl);
            config.put("content_type", "json");
            config.put("secret", repoWebhookSecret);
            webhookPayload.put("config", config);
            
            // Events to listen for - only pull_request for task linking
            webhookPayload.put("events", Arrays.asList("pull_request"));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(webhookPayload, headers);
            String webhooksUrl = GithubConstants.getWebhooksUrl(owner, repoName);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhooksUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                Long hookId = jsonResponse.get("id").asLong();
                
                gitHubRepo.setWebhookId(hookId);
                gitHubRepo.setWebhookUrl(webhookUrl);
                gitHubRepo.setWebhookSecret(repoWebhookSecret);
                gitHubRepo.setWebhookActive(true);
                repo.save(gitHubRepo);
            } else {
                throw new ServiceException(ErrorConstants.GITHUB_WEBHOOK_CREATE_FAILED);
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new ServiceException(ErrorConstants.GITHUB_REPO_ACCESS_DENIED, e);
            }
            throw new ServiceException(ErrorConstants.GITHUB_WEBHOOK_CREATE_FAILED, e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ErrorConstants.GITHUB_WEBHOOK_CREATE_FAILED, e);
        }
    }

    /**
     * Generate a random webhook secret for signature verification.
     */
    private String generateWebhookSecret() {
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Delete a webhook from a GitHub repository.
     */
    @Transactional
    public GitHubRepo deleteWebhook(Long projectId, Long repoId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageGitHubRepos(project, userId);

        GitHubRepo gitHubRepo = repo.findByProjectIdAndId(projectId, repoId)
                .orElseThrow(() -> new ServiceException(ErrorConstants.GITHUB_REPO_NOT_FOUND));

        if (!gitHubRepo.isWebhookActive() || gitHubRepo.getWebhookId() == null) {
            // No webhook to delete
            return gitHubRepo;
        }

        deleteWebhook(gitHubRepo);
        return gitHubRepo;
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

    private void deleteWebhook(GitHubRepo gitHubRepo) {
        String owner = gitHubRepo.getOwner();
        String repoName = gitHubRepo.getRepoName();

        try {
            HttpHeaders headers = createAuthHeaders(gitHubRepo.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(headers);
            String webhookUrl = GithubConstants.getWebhookUrl(owner, repoName, gitHubRepo.getWebhookId());

            restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.DELETE,
                    request,
                    String.class
            );

            gitHubRepo.setWebhookId(null);
            gitHubRepo.setWebhookUrl(null);
            gitHubRepo.setWebhookActive(false);
            repo.save(gitHubRepo);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Webhook already deleted on GitHub
                gitHubRepo.setWebhookId(null);
                gitHubRepo.setWebhookUrl(null);
                gitHubRepo.setWebhookActive(false);
                repo.save(gitHubRepo);
            } else {
                throw new ServiceException(ErrorConstants.GITHUB_WEBHOOK_DELETE_FAILED, e);
            }
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
