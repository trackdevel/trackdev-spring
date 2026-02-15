package org.trackdev.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.ActivityType;
import org.trackdev.api.entity.GitHubRepo;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.prchanges.PullRequestChange;
import org.trackdev.api.entity.prchanges.PullRequestClosedChange;
import org.trackdev.api.entity.prchanges.PullRequestEditedChange;
import org.trackdev.api.entity.prchanges.PullRequestMergedChange;
import org.trackdev.api.entity.prchanges.PullRequestOpenedChange;
import org.trackdev.api.entity.prchanges.PullRequestReopenedChange;
import org.trackdev.api.entity.prchanges.PullRequestSynchronizeChange;
import org.trackdev.api.repository.PullRequestRepository;
import org.trackdev.api.dto.PRFileDetailDTO;
import org.trackdev.api.utils.ErrorConstants;
import org.trackdev.api.utils.GithubConstants;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PullRequestService extends BaseServiceUUID<PullRequest, PullRequestRepository> {

    private static final Logger log = LoggerFactory.getLogger(PullRequestService.class);

    @Autowired
    TaskService taskService;

    @Autowired
    UserService userService;

    @Autowired
    PullRequestChangeService pullRequestChangeService;

    @Autowired
    ActivityService activityService;

    @Autowired
    GitHubRepoService gitHubRepoService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PullRequestService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public PullRequest create(String prNodeId, String url, Long taskId) {
        PullRequest pr = new PullRequest(url, prNodeId);
        this.repo.save(pr);
        if (taskId != null) {
            Task task = taskService.get(taskId);
            task.addPullRequest(pr);
        }
        return pr;
    }

    /**
     * Process a GitHub webhook event for a pull request.
     * Always creates/updates the PR and records a change event.
     * This is called for every webhook, regardless of task linking.
     * 
     * @param prUrl The PR URL
     * @param nodeId The GitHub node ID
     * @param prNumber The PR number
     * @param title The PR title
     * @param body The PR body/description
     * @param state The PR state (open, closed)
     * @param merged Whether the PR has been merged
     * @param repoFullName The repository full name (owner/repo)
     * @param authorLogin The PR author's GitHub login
     * @param action The webhook action (opened, closed, reopened, etc.)
     * @param senderLogin The GitHub user who triggered the action
     * @return The created or updated PullRequest
     */
    @Transactional
    public PullRequest processWebhookEvent(String prUrl, String nodeId,
                                           Integer prNumber, String title, String body,
                                           String state, Boolean merged, String repoFullName,
                                           String authorLogin, String action, String senderLogin) {
        // Find or create the PR by URL
        Optional<PullRequest> existingPR = this.repo.findByUrl(prUrl);
        PullRequest pr;
        boolean isNewPR = existingPR.isEmpty();
        
        if (existingPR.isPresent()) {
            pr = existingPR.get();
            // Update existing PR with latest data
            pr.setTitle(title);
            pr.setState(state);
            pr.setMerged(merged);
            pr.setUpdatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        } else {
            // Create new PR
            pr = new PullRequest(prUrl, nodeId != null ? nodeId : generateNodeId(prUrl));
            pr.setPrNumber(prNumber);
            pr.setTitle(title);
            pr.setState(state);
            pr.setMerged(merged);
            pr.setRepoFullName(repoFullName);
            pr.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            
            // Try to find author by GitHub login or username
            if (authorLogin != null) {
                User author = userService.findByGithubUsernameOrUsername(authorLogin);
                if (author != null) {
                    pr.setAuthor(author);
                }
            }
        }
        
        this.repo.save(pr);
        
        // Always record the change event for this webhook action
        recordPullRequestChange(pr, action, senderLogin, title, prNumber, repoFullName, merged);
        
        return pr;
    }

    /**
     * Link an existing pull request to a task by task key.
     * The PR must already exist (created via processWebhookEvent).
     * 
     * @param taskKey The task key (e.g., "a7k-1")
     * @param pr The existing PullRequest to link
     * @param action The webhook action for activity recording
     * @param senderLogin The GitHub user who triggered the action
     */
    @Transactional
    public void linkPullRequestToTask(String taskKey, PullRequest pr, String action, String senderLogin) {
        // Find the task by key
        Task task = taskService.findByTaskKey(taskKey)
                .orElseThrow(() -> new EntityNotFound("Task", taskKey));
        
        // Check if already linked to this task
        boolean isNewLink = !pr.hasTask(task);
        if (isNewLink) {
            // Add this task to the PR's tasks (many-to-many)
            task.addPullRequest(pr);
            this.repo.save(pr);
            
            // Record activity for the new link
            recordPullRequestActivity(pr, task, action, pr.getMerged(), senderLogin);
        }
    }

    /**
     * Link a pull request to a task by task key.
     * If the PR already exists (by URL), updates it. Otherwise creates a new one.
     * Records change history for tracking.
     * 
     * @deprecated Use processWebhookEvent + linkPullRequestToTask instead
     * 
     * @param taskKey The task key (e.g., "a7k-1")
     * @param prUrl The PR URL
     * @param nodeId The GitHub node ID
     * @param prNumber The PR number
     * @param title The PR title
     * @param state The PR state (open, closed)
     * @param merged Whether the PR has been merged
     * @param repoFullName The repository full name (owner/repo)
     * @param authorLogin The PR author's GitHub login
     * @param action The webhook action (opened, closed, reopened, etc.)
     * @param senderLogin The GitHub user who triggered the action
     * @return The linked or updated PullRequest
     */
    @Deprecated
    @Transactional
    public PullRequest linkPullRequestToTask(String taskKey, String prUrl, String nodeId,
                                              Integer prNumber, String title, String state,
                                              Boolean merged, String repoFullName, String authorLogin,
                                              String action, String senderLogin,
                                              boolean recordChange) {
        // Find the task by key
        Task task = taskService.findByTaskKey(taskKey)
                .orElseThrow(() -> new EntityNotFound("Task", taskKey));
        
        // Find or create the PR
        Optional<PullRequest> existingPR = this.repo.findByUrl(prUrl);
        PullRequest pr;
        boolean isNewPR = existingPR.isEmpty();
        boolean isNewLink = true;
        
        if (existingPR.isPresent()) {
            pr = existingPR.get();
            // Update existing PR
            pr.setTitle(title);
            pr.setState(state);
            pr.setMerged(merged);
            pr.setUpdatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            
            // Check if already linked to this task
            isNewLink = !pr.hasTask(task);
            if (isNewLink) {
                // Add this task to the PR's tasks (many-to-many)
                task.addPullRequest(pr);
            }
        } else {
            // Create new PR
            pr = new PullRequest(prUrl, nodeId != null ? nodeId : generateNodeId(prUrl));
            pr.setPrNumber(prNumber);
            pr.setTitle(title);
            pr.setState(state);
            pr.setMerged(merged);
            pr.setRepoFullName(repoFullName);
            pr.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            
            // Try to find author by GitHub login or username
            if (authorLogin != null) {
                User author = userService.findByGithubUsernameOrUsername(authorLogin);
                if (author != null) {
                    pr.setAuthor(author);
                }
            }
            
            // Save PR first, then link to task (for many-to-many)
            this.repo.save(pr);
            task.addPullRequest(pr);
        }
        
        this.repo.save(pr);
        
        // Record change history based on action - only once per PR, not per task
        if (recordChange) {
            recordPullRequestChange(pr, action, senderLogin, title, prNumber, repoFullName, merged);
            // Record activity for all linked tasks
            recordPullRequestActivity(pr, task, action, merged, senderLogin);
        }
        
        return pr;
    }

    /**
     * Record activity for pull request events.
     */
    private void recordPullRequestActivity(PullRequest pr, Task task, String action, Boolean merged, String senderLogin) {
        User actor = userService.findByGithubUsernameOrUsername(senderLogin);
        if (actor == null) {
            // Actor not found - skip activity recording
            return;
        }
        
        ActivityType activityType = null;
        String message = pr.getTitle();
        
        switch (action) {
            case "opened":
                activityType = ActivityType.PR_LINKED;
                break;
            case "closed":
                if (Boolean.TRUE.equals(merged)) {
                    activityType = ActivityType.PR_MERGED;
                } else {
                    activityType = ActivityType.PR_STATE_CHANGED;
                }
                break;
            case "reopened":
                activityType = ActivityType.PR_STATE_CHANGED;
                break;
        }
        
        if (activityType != null && task != null) {
            activityService.recordActivity(activityType, actor, task.getProject(), task, 
                    message, null, action);
        }
    }

    /**
     * Record a change to a pull request based on the action.
     * Always records a change for every webhook event.
     */
    private void recordPullRequestChange(PullRequest pr, String action, String senderLogin,
                                          String title, Integer prNumber, String repoFullName, Boolean merged) {
        PullRequestChange change = null;
        
        // Try to find the system user who triggered this action
        User systemUser = userService.findByGithubUsernameOrUsername(senderLogin);
        
        switch (action) {
            case "opened":
                change = new PullRequestOpenedChange(systemUser, pr, senderLogin, title, prNumber, repoFullName);
                break;
            case "closed":
                if (Boolean.TRUE.equals(merged)) {
                    // PR was merged - record as merge event
                    change = new PullRequestMergedChange(systemUser, pr, senderLogin, senderLogin);
                } else {
                    // PR was closed without merge
                    change = new PullRequestClosedChange(systemUser, pr, senderLogin, false, null);
                }
                break;
            case "reopened":
                change = new PullRequestReopenedChange(systemUser, pr, senderLogin);
                break;
            case "synchronize":
                change = new PullRequestSynchronizeChange(systemUser, pr, senderLogin);
                break;
            case "edited":
                change = new PullRequestEditedChange(systemUser, pr, senderLogin, title);
                break;
            // For other action types, we still want to record the event but with a generic type
            default:
                // Log but don't create a specific change entity for unsupported actions
                break;
        }
        
        if (change != null) {
            pullRequestChangeService.save(change);
        }
    }

    /**
     * Get change history for a pull request.
     */
    public List<PullRequestChange> getPullRequestHistory(String pullRequestId) {
        PullRequest pr = get(pullRequestId);
        return pullRequestChangeService.findByPullRequestOrderByChangedAtDesc(pr);
    }

    /**
     * Get change history for all pull requests linked to a task.
     */
    public List<PullRequestChange> getPullRequestHistoryForTask(Long taskId) {
        Task task = taskService.get(taskId);
        List<PullRequest> prs = task.getPullRequests().stream().toList();
        return pullRequestChangeService.findByPullRequestInOrderByChangedAtDesc(prs);
    }

    /**
     * Generate a synthetic node ID from URL if not provided.
     */
    private String generateNodeId(String url) {
        return "PR_" + Math.abs(url.hashCode());
    }

    @Transactional
    public PullRequest findOrCreateByNodeId(String url, String nodeId, String login) {
        User author = userService.findByGithubUsernameOrUsername(login);
        
        Optional<PullRequest> opr = this.repo().findByNodeId(nodeId);
        if (opr.isEmpty()) {
            PullRequest pr = new PullRequest(url, nodeId);
            if (author != null) {
                pr.setAuthor(author);
            }
            this.repo().save(pr);
            return pr;
        }
        return opr.get();
    }

    public PullRequest getByNodeId(String prNodeId) {
        return this.repo().findByNodeId(prNodeId).orElseThrow(
                () -> new ServiceException("PullRequest with node_id = %s does not exist".formatted(prNodeId)));
    }

    /**
     * Fetch PR statistics (additions, deletions, changedFiles) from GitHub API.
     * Uses the repository's access token for authentication.
     * Updates the PR entity with the fetched stats.
     * 
     * @param pr The PullRequest to fetch stats for
     * @return The updated PullRequest with stats, or the original if fetch fails
     */
    @Transactional
    public PullRequest fetchAndUpdatePRStats(PullRequest pr) {
        if (pr.getRepoFullName() == null || pr.getPrNumber() == null) {
            log.warn("Cannot fetch PR stats - missing repoFullName or prNumber for PR {}", pr.getId());
            return pr;
        }

        // Parse owner and repo from repoFullName (format: "owner/repo")
        String[] parts = pr.getRepoFullName().split("/");
        if (parts.length != 2) {
            log.warn("Invalid repoFullName format for PR {}: {}", pr.getId(), pr.getRepoFullName());
            return pr;
        }
        String owner = parts[0];
        String repoName = parts[1];

        // Find a GitHub repo with access token for this repository
        Optional<GitHubRepo> gitHubRepo = gitHubRepoService.findByUrl("https://github.com/" + pr.getRepoFullName());
        if (gitHubRepo.isEmpty()) {
            // Try alternative URL format
            gitHubRepo = gitHubRepoService.findByUrl("https://github.com/" + pr.getRepoFullName() + ".git");
        }

        String accessToken = gitHubRepo.map(GitHubRepo::getAccessToken).orElse(null);
        if (accessToken == null) {
            log.warn("No access token found for repository {}", pr.getRepoFullName());
            return pr;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String prUrl = GithubConstants.getPullUrl(owner, repoName, pr.getPrNumber());
            log.debug("Fetching PR stats from: {}", prUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    prUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                // Extract basic PR info
                if (jsonResponse.has("title")) {
                    pr.setTitle(jsonResponse.get("title").asText());
                }
                if (jsonResponse.has("state")) {
                    pr.setState(jsonResponse.get("state").asText());
                }
                if (jsonResponse.has("merged")) {
                    pr.setMerged(jsonResponse.get("merged").asBoolean());
                }
                
                // Extract stats from response
                if (jsonResponse.has("additions")) {
                    pr.setAdditions(jsonResponse.get("additions").asInt());
                }
                if (jsonResponse.has("deletions")) {
                    pr.setDeletions(jsonResponse.get("deletions").asInt());
                }
                if (jsonResponse.has("changed_files")) {
                    pr.setChangedFiles(jsonResponse.get("changed_files").asInt());
                }
                pr.setStatsFetchedAt(ZonedDateTime.now(ZoneId.of("UTC")));
                
                this.repo.save(pr);
                log.info("Updated PR #{} - title: '{}', merged: {}, +{} -{} files:{}", 
                        pr.getPrNumber(), pr.getTitle(), pr.getMerged(), 
                        pr.getAdditions(), pr.getDeletions(), pr.getChangedFiles());
            }
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch PR stats for PR #{} in {}: {} - {}", 
                    pr.getPrNumber(), pr.getRepoFullName(), e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching PR stats for PR #{} in {}: {}", 
                    pr.getPrNumber(), pr.getRepoFullName(), e.getMessage());
        }

        return pr;
    }

    /**
     * Fetch stats for all PRs linked to tasks in a project.
     * Only fetches stats for PRs that don't have stats yet or were fetched more than 1 hour ago.
     * 
     * @param projectId The project ID
     * @return List of updated PRs
     */
    @Transactional
    public List<PullRequest> fetchPRStatsForProject(Long projectId) {
        // Get all tasks in the project with status DONE
        List<Task> doneTasks = taskService.findByProjectIdAndStatus(projectId, TaskStatus.DONE);
        
        ZonedDateTime oneHourAgo = ZonedDateTime.now(ZoneId.of("UTC")).minusHours(1);
        
        return doneTasks.stream()
                .flatMap(task -> task.getPullRequests().stream())
                .filter(pr -> pr.getStatsFetchedAt() == null || pr.getStatsFetchedAt().isBefore(oneHourAgo))
                .distinct()
                .map(this::fetchAndUpdatePRStats)
                .toList();
    }

    /**
     * Compute the number of surviving lines for a PR.
     * Surviving lines are lines added by this PR that still exist unchanged in the main branch.
     * 
     * This is computed by:
     * 1. Getting the merge commit SHA (for squash/rebase merges) and PR commit SHAs
     * 2. Getting all files changed in the PR
     * 3. For each file, getting the blame from the main branch
     * 4. Counting lines where the commit SHA matches a PR commit or merge commit
     * 
     * Note: For squash merges, only the merge commit SHA will match.
     * For regular merges, the original PR commit SHAs will match.
     * 
     * @param pr The pull request to analyze
     * @return The number of surviving lines, or null if unable to compute
     */
    public Integer computeSurvivingLines(PullRequest pr) {
        if (pr.getRepoFullName() == null || pr.getPrNumber() == null) {
            return null;
        }
        
        // Only compute for merged PRs
        if (!pr.isMerged()) {
            log.debug("PR #{} is not merged, skipping surviving lines calculation", pr.getPrNumber());
            return null;
        }
        
        String[] parts = pr.getRepoFullName().split("/");
        if (parts.length != 2) {
            return null;
        }
        String owner = parts[0];
        String repoName = parts[1];
        
        // Find access token for this repository
        Optional<GitHubRepo> gitHubRepo = gitHubRepoService.findByUrl("https://github.com/" + pr.getRepoFullName());
        if (gitHubRepo.isEmpty()) {
            gitHubRepo = gitHubRepoService.findByUrl("https://github.com/" + pr.getRepoFullName() + ".git");
        }
        
        String accessToken = gitHubRepo.map(GitHubRepo::getAccessToken).orElse(null);
        if (accessToken == null) {
            log.warn("No access token found for repository {}", pr.getRepoFullName());
            return null;
        }
        
        try {
            // Step 1: Get the merge commit SHA and original PR commits
            Set<String> relevantCommitShas = new HashSet<>();
            
            // Fetch merge commit SHA from PR API (critical for squash/rebase merges)
            String mergeCommitSha = fetchMergeCommitSha(owner, repoName, pr.getPrNumber(), accessToken);
            if (mergeCommitSha != null) {
                relevantCommitShas.add(mergeCommitSha);
                log.debug("Found merge commit SHA for PR #{}: {}", pr.getPrNumber(), mergeCommitSha);
            }
            
            // Also get original PR commits (for regular merge commits)
            Set<String> prCommitShas = fetchPRCommitShas(owner, repoName, pr.getPrNumber(), accessToken);
            relevantCommitShas.addAll(prCommitShas);
            
            if (relevantCommitShas.isEmpty()) {
                log.warn("No commits found for PR #{} in {}", pr.getPrNumber(), pr.getRepoFullName());
                return null;
            }
            log.debug("Found {} relevant commits for PR #{} (merge + original)", 
                    relevantCommitShas.size(), pr.getPrNumber());
            
            // Step 2: Get all files changed in the PR
            List<String> changedFiles = fetchPRFiles(owner, repoName, pr.getPrNumber(), accessToken);
            if (changedFiles.isEmpty()) {
                log.debug("No files found for PR #{}", pr.getPrNumber());
                return 0;
            }
            log.debug("Found {} files changed in PR #{}", changedFiles.size(), pr.getPrNumber());
            
            // Step 3: For each file, get blame and count surviving lines
            int totalSurvivingLines = 0;
            for (String filePath : changedFiles) {
                int survivingInFile = countSurvivingLinesInFile(owner, repoName, filePath, relevantCommitShas, accessToken);
                totalSurvivingLines += survivingInFile;
            }
            
            log.info("PR #{} in {} has {} surviving lines out of {} additions", 
                    pr.getPrNumber(), pr.getRepoFullName(), totalSurvivingLines, pr.getAdditions());
            
            return totalSurvivingLines;
            
        } catch (Exception e) {
            log.error("Error computing surviving lines for PR #{} in {}: {}", 
                    pr.getPrNumber(), pr.getRepoFullName(), e.getMessage());
            return null;
        }
    }

    /**
     * Fetch the merge commit SHA for a merged pull request.
     * This is critical for squash and rebase merges where the original commits are replaced.
     * 
     * @return The merge commit SHA, or null if not found or PR is not merged
     */
    private String fetchMergeCommitSha(String owner, String repo, int prNumber, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = GithubConstants.getPullUrl(owner, repo, prNumber);
            log.debug("Fetching PR merge commit from: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode prData = objectMapper.readTree(response.getBody());
                
                // Check if PR is merged
                boolean merged = prData.path("merged").asBoolean(false);
                if (!merged) {
                    log.debug("PR #{} is not merged", prNumber);
                    return null;
                }
                
                // Get merge commit SHA
                if (prData.has("merge_commit_sha") && !prData.get("merge_commit_sha").isNull()) {
                    return prData.get("merge_commit_sha").asText();
                }
            }
        } catch (Exception e) {
            log.error("Error fetching merge commit SHA for PR #{}: {}", prNumber, e.getMessage());
        }
        
        return null;
    }

    /**
     * Fetch all commit SHAs from a pull request.
     */
    private Set<String> fetchPRCommitShas(String owner, String repo, int prNumber, String accessToken) {
        Set<String> commitShas = new HashSet<>();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = GithubConstants.getPullCommitsUrl(owner, repo, prNumber);
            log.debug("Fetching PR commits from: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode commits = objectMapper.readTree(response.getBody());
                for (JsonNode commit : commits) {
                    if (commit.has("sha")) {
                        commitShas.add(commit.get("sha").asText());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching PR commits: {}", e.getMessage());
        }
        
        return commitShas;
    }

    /**
     * Fetch all file paths changed in a pull request.
     */
    private List<String> fetchPRFiles(String owner, String repo, int prNumber, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = GithubConstants.getPullFilesUrl(owner, repo, prNumber);
            log.debug("Fetching PR files from: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode files = objectMapper.readTree(response.getBody());
                return java.util.stream.StreamSupport.stream(files.spliterator(), false)
                        .filter(file -> file.has("filename") && !"removed".equals(file.path("status").asText()))
                        .map(file -> file.get("filename").asText())
                        .toList();
            }
        } catch (Exception e) {
            log.error("Error fetching PR files: {}", e.getMessage());
        }
        
        return List.of();
    }

    /**
     * Count surviving lines in a file by checking blame against PR commits.
     * Uses GitHub GraphQL API to get blame information.
     */
    private int countSurvivingLinesInFile(String owner, String repo, String filePath, 
                                           Set<String> prCommitShas, String accessToken) {
        try {
            // GraphQL query to get blame information
            // Note: blame is on Commit (not Blob), and path is passed as argument
            String query = """
                query {
                  repository(owner: "%s", name: "%s") {
                    defaultBranchRef {
                      target {
                        ... on Commit {
                          blame(path: "%s") {
                            ranges {
                              startingLine
                              endingLine
                              commit {
                                oid
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(owner, repo, filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> body = new HashMap<>();
            body.put("query", query);
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    GithubConstants.GITHUB_GRAPHQL_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                // Check for errors
                if (jsonResponse.has("errors")) {
                    log.debug("GraphQL error for file {}: {}", filePath, jsonResponse.get("errors"));
                    return 0;
                }
                
                JsonNode blame = jsonResponse.path("data").path("repository")
                        .path("defaultBranchRef").path("target").path("blame");
                if (blame.isMissingNode()) {
                    // File might have been deleted or doesn't exist in HEAD
                    log.debug("No blame data for file {}", filePath);
                    return 0;
                }
                
                JsonNode ranges = blame.path("ranges");
                int survivingLines = 0;
                
                for (JsonNode range : ranges) {
                    String commitOid = range.path("commit").path("oid").asText();
                    if (prCommitShas.contains(commitOid)) {
                        int startLine = range.path("startingLine").asInt();
                        int endLine = range.path("endingLine").asInt();
                        survivingLines += (endLine - startLine + 1);
                    }
                }
                
                log.debug("File {} has {} surviving lines from PR", filePath, survivingLines);
                return survivingLines;
            }
        } catch (Exception e) {
            log.debug("Error getting blame for file {}: {}", filePath, e.getMessage());
        }
        
        return 0;
    }

    /**
     * Compute surviving lines for all PRs of a task and return as a map.
     * 
     * @param prs Collection of pull requests
     * @return Map of PR ID to surviving lines count
     */
    public Map<String, Integer> computeSurvivingLinesForPRs(Iterable<PullRequest> prs) {
        Map<String, Integer> result = new HashMap<>();
        for (PullRequest pr : prs) {
            Integer survivingLines = computeSurvivingLines(pr);
            if (survivingLines != null) {
                result.put(pr.getId(), survivingLines);
            }
        }
        return result;
    }

    /**
     * Get detailed file-level analysis for a pull request, including surviving and deleted lines per file.
     * 
     * @param prId The pull request ID
     * @return List of file details with line ranges, or empty list if unable to compute
     */
    public List<PRFileDetailDTO> getFileDetails(String prId) {
        PullRequest pr = get(prId);
        if (pr == null) {
            throw new EntityNotFound(ErrorConstants.ENTITY_NOT_EXIST);
        }
        
        if (pr.getRepoFullName() == null || pr.getPrNumber() == null) {
            return List.of();
        }
        
        // Only compute for merged PRs
        if (!pr.isMerged()) {
            log.debug("PR #{} is not merged, returning empty file details", pr.getPrNumber());
            return List.of();
        }
        
        String[] parts = pr.getRepoFullName().split("/");
        if (parts.length != 2) {
            return List.of();
        }
        String owner = parts[0];
        String repoName = parts[1];
        
        // Find access token for this repository
        Optional<GitHubRepo> gitHubRepo = gitHubRepoService.findByUrl("https://github.com/" + pr.getRepoFullName());
        if (gitHubRepo.isEmpty()) {
            gitHubRepo = gitHubRepoService.findByUrl("https://github.com/" + pr.getRepoFullName() + ".git");
        }
        
        String accessToken = gitHubRepo.map(GitHubRepo::getAccessToken).orElse(null);
        if (accessToken == null) {
            log.warn("No access token found for repository {}", pr.getRepoFullName());
            return List.of();
        }
        
        try {
            // Get relevant commit SHAs
            Set<String> relevantCommitShas = new HashSet<>();
            
            String mergeCommitSha = fetchMergeCommitSha(owner, repoName, pr.getPrNumber(), accessToken);
            if (mergeCommitSha != null) {
                relevantCommitShas.add(mergeCommitSha);
            }
            
            Set<String> prCommitShas = fetchPRCommitShas(owner, repoName, pr.getPrNumber(), accessToken);
            relevantCommitShas.addAll(prCommitShas);
            
            if (relevantCommitShas.isEmpty()) {
                return List.of();
            }
            
            // Get files with stats
            List<PRFileDetailDTO> fileDetails = fetchPRFilesWithStats(owner, repoName, pr.getPrNumber(), accessToken);
            
            // Get PR author info for non-surviving lines
            String prAuthorFullName = pr.getAuthor() != null ? pr.getAuthor().getFullName() : null;
            String prAuthorGithubUsername = pr.getAuthor() != null && pr.getAuthor().getGithubInfo() != null 
                    ? pr.getAuthor().getGithubInfo().getLogin() : null;
            
            // Process files in parallel to speed up analysis (2 API calls per file: content + blame)
            // Use a limited thread pool to respect GitHub rate limits
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, fileDetails.size()));
            try {
                // Create final copies for lambda
                final Set<String> finalRelevantCommitShas = relevantCommitShas;
                final String finalMergeCommitSha = mergeCommitSha;
                
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (PRFileDetailDTO fileDetail : fileDetails) {
                    if (!"deleted".equals(fileDetail.getStatus())) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            populateFileLineDetails(owner, repoName, pr.getPrNumber(), fileDetail, 
                                    finalRelevantCommitShas, finalMergeCommitSha, accessToken, 
                                    prAuthorFullName, prAuthorGithubUsername);
                        }, executor);
                        futures.add(future);
                    }
                }
                
                // Wait for all files to be processed
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } finally {
                executor.shutdown();
            }
            
            return fileDetails;
            
        } catch (Exception e) {
            log.error("Error getting file details for PR #{} in {}: {}", 
                    pr.getPrNumber(), pr.getRepoFullName(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Fetch PR files with their stats (additions, deletions, status) and patch.
     */
    private List<PRFileDetailDTO> fetchPRFilesWithStats(String owner, String repo, int prNumber, String accessToken) {
        List<PRFileDetailDTO> files = new ArrayList<>();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = GithubConstants.getPullFilesUrl(owner, repo, prNumber);
            log.debug("Fetching PR files from: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode filesArray = objectMapper.readTree(response.getBody());
                for (JsonNode fileNode : filesArray) {
                    PRFileDetailDTO fileDetail = new PRFileDetailDTO();
                    fileDetail.setFilePath(fileNode.path("filename").asText());
                    fileDetail.setStatus(fileNode.path("status").asText());
                    fileDetail.setAdditions(fileNode.path("additions").asInt(0));
                    fileDetail.setDeletions(fileNode.path("deletions").asInt(0));
                    fileDetail.setSurvivingLines(0);
                    fileDetail.setDeletedLines(0);
                    fileDetail.setLines(new ArrayList<>());
                    // Store the patch for line-level analysis
                    String patch = fileNode.path("patch").asText(null);
                    fileDetail.setPatch(patch);
                    files.add(fileDetail);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching PR files: {}", e.getMessage());
        }
        
        return files;
    }

    /**
     * Fetch file content at a specific commit.
     */
    private String fetchFileContentAtCommit(String owner, String repo, String filePath, String commitSha, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.raw+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // GitHub API to get file content at a specific ref
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", 
                    owner, repo, filePath, commitSha);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (HttpClientErrorException.NotFound e) {
            // File doesn't exist at this commit (could be a new file)
            log.debug("File {} not found at commit {}", filePath, commitSha);
        } catch (Exception e) {
            log.debug("Error fetching file content for {}: {}", filePath, e.getMessage());
        }
        return null;
    }

    /**
     * Fetch current file content from default branch.
     */
    private String fetchCurrentFileContent(String owner, String repo, String filePath, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.raw+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, filePath);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("File {} not found in current branch", filePath);
        } catch (Exception e) {
            log.debug("Error fetching current file content for {}: {}", filePath, e.getMessage());
        }
        return null;
    }

    /**
     * Holds PR information for a commit.
     */
    private static class CommitPrInfo {
        final Integer prNumber;
        final String prUrl;
        
        CommitPrInfo(Integer prNumber, String prUrl) {
            this.prNumber = prNumber;
            this.prUrl = prUrl;
        }
    }

    /**
     * Fetch PRs associated with given commit SHAs.
     * Returns a map from commit SHA to PR info (number and URL).
     * Only returns the first (most recent) merged PR for each commit.
     */
    private Map<String, CommitPrInfo> fetchPRsForCommits(String owner, String repo, 
            Set<String> commitShas, String accessToken) {
        Map<String, CommitPrInfo> result = new HashMap<>();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        
        for (String commitSha : commitShas) {
            try {
                String url = String.format("https://api.github.com/repos/%s/%s/commits/%s/pulls", 
                        owner, repo, commitSha);
                
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, requestEntity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    JsonNode prs = objectMapper.readTree(response.getBody());
                    if (prs.isArray() && !prs.isEmpty()) {
                        // Get the first merged PR (or any PR if none merged)
                        for (JsonNode pr : prs) {
                            if ("closed".equals(pr.path("state").asText()) && 
                                !pr.path("merged_at").isNull()) {
                                result.put(commitSha, new CommitPrInfo(
                                        pr.path("number").asInt(),
                                        pr.path("html_url").asText()
                                ));
                                break;
                            }
                        }
                        // If no merged PR found, use first PR
                        if (!result.containsKey(commitSha) && !prs.isEmpty()) {
                            JsonNode firstPr = prs.get(0);
                            result.put(commitSha, new CommitPrInfo(
                                    firstPr.path("number").asInt(),
                                    firstPr.path("html_url").asText()
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Error fetching PRs for commit {}: {}", commitSha, e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * Populate line-level detail for a file using blame information and file content.
     * Creates an interleaved view showing:
     * - All current lines with their line numbers (marked as SURVIVING if from PR, CURRENT otherwise)
     * - Deleted lines from the PR inserted at their approximate original positions (no line number)
     */
    private void populateFileLineDetails(String owner, String repo, int prNumber, PRFileDetailDTO fileDetail, 
                                          Set<String> prCommitShas, String mergeCommitSha, String accessToken,
                                          String prAuthorFullName, String prAuthorGithubUsername) {
        try {
            int survivingCount = 0;
            int nonSurvivingCount = 0;
            
            // Generate the PR file URL - format: https://github.com/owner/repo/pull/number/files
            String prFileUrl = String.format("https://github.com/%s/%s/pull/%d/files#diff-%s", 
                    owner, repo, prNumber, 
                    java.util.Base64.getEncoder().encodeToString(fileDetail.getFilePath().getBytes()).replace("=", ""));
            
            String patch = fileDetail.getPatch();
            if (patch == null || patch.isEmpty()) {
                fileDetail.setLines(new ArrayList<>());
                fileDetail.setSurvivingLines(0);
                fileDetail.setDeletedLines(0);
                return;
            }
            
            // Step 1: Parse the patch to extract lines ADDED by this PR with their context
            List<PatchLineInfo> patchAddedLines = new ArrayList<>();
            String[] patchLines = patch.split("\n");
            int newLineNumber = 0;
            
            for (String patchLine : patchLines) {
                if (patchLine.startsWith("@@")) {
                    java.util.regex.Matcher matcher = java.util.regex.Pattern
                            .compile("@@ -\\d+(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@")
                            .matcher(patchLine);
                    if (matcher.find()) {
                        newLineNumber = Integer.parseInt(matcher.group(1));
                    }
                } else if (patchLine.startsWith("+") && !patchLine.startsWith("+++")) {
                    String content = patchLine.substring(1);
                    patchAddedLines.add(new PatchLineInfo(newLineNumber, content));
                    newLineNumber++;
                } else if (patchLine.startsWith("-") && !patchLine.startsWith("---")) {
                    // Deleted line - don't increment newLineNumber
                } else if (!patchLine.startsWith("\\")) {
                    newLineNumber++;
                }
            }
            
            // Step 2: Get current file content
            String currentContent = fetchCurrentFileContent(owner, repo, fileDetail.getFilePath(), accessToken);
            String[] currentLines = currentContent != null ? currentContent.split("\n", -1) : new String[0];
            
            // Step 3: Use blame API to get commit attribution AND author info for current lines
            Map<Integer, String> currentLineToCommit = new HashMap<>();
            Map<Integer, String> currentLineToAuthor = new HashMap<>(); // line number -> GitHub username
            Set<Integer> survivingLineNumbers = new HashSet<>();
            
            // Expanded query to include author login
            String query = """
                query {
                  repository(owner: "%s", name: "%s") {
                    defaultBranchRef {
                      target {
                        ... on Commit {
                          blame(path: "%s") {
                            ranges {
                              startingLine
                              endingLine
                              commit {
                                oid
                                author {
                                  user {
                                    login
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(owner, repo, fileDetail.getFilePath());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> body = new HashMap<>();
            body.put("query", query);
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    GithubConstants.GITHUB_GRAPHQL_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // Cache for user lookups to avoid repeated database queries
            Map<String, String> githubUsernameToFullName = new HashMap<>();
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                if (!jsonResponse.has("errors")) {
                    JsonNode blame = jsonResponse.path("data").path("repository")
                            .path("defaultBranchRef").path("target").path("blame");
                    
                    if (!blame.isMissingNode()) {
                        JsonNode ranges = blame.path("ranges");
                        for (JsonNode range : ranges) {
                            String commitOid = range.path("commit").path("oid").asText();
                            String authorLogin = range.path("commit").path("author").path("user").path("login").asText(null);
                            int startLine = range.path("startingLine").asInt();
                            int endLine = range.path("endingLine").asInt();
                            
                            for (int line = startLine; line <= endLine; line++) {
                                currentLineToCommit.put(line, commitOid);
                                if (authorLogin != null) {
                                    currentLineToAuthor.put(line, authorLogin);
                                }
                                if (prCommitShas.contains(commitOid)) {
                                    survivingLineNumbers.add(line);
                                }
                            }
                        }
                    }
                }
            }
            
            // Step 4: Build map of surviving line contents to their current line numbers
            Map<String, List<Integer>> survivingContentToLineNums = new HashMap<>();
            for (Integer lineNum : survivingLineNumbers) {
                if (lineNum > 0 && lineNum <= currentLines.length) {
                    String content = currentLines[lineNum - 1].trim();
                    survivingContentToLineNums.computeIfAbsent(content, k -> new ArrayList<>()).add(lineNum);
                }
            }
            Set<String> survivingLineContents = survivingContentToLineNums.keySet();
            
            // Step 5: Classify patch added lines as surviving or non-surviving
            Map<Integer, List<PatchLineInfo>> insertAfterLine = new HashMap<>();
            List<PatchLineInfo> insertAtEnd = new ArrayList<>();
            List<PatchLineInfo> insertAtStart = new ArrayList<>();
            
            for (int i = 0; i < patchAddedLines.size(); i++) {
                PatchLineInfo patchLine = patchAddedLines.get(i);
                String trimmedContent = patchLine.content.trim();
                
                if (trimmedContent.isEmpty() || survivingLineContents.contains(trimmedContent)) {
                    continue;
                }
                
                Integer insertPoint = null;
                for (int j = i - 1; j >= 0; j--) {
                    String neighborContent = patchAddedLines.get(j).content.trim();
                    if (!neighborContent.isEmpty() && survivingContentToLineNums.containsKey(neighborContent)) {
                        List<Integer> lineNums = survivingContentToLineNums.get(neighborContent);
                        insertPoint = lineNums.get(0);
                        break;
                    }
                }
                
                if (insertPoint != null) {
                    insertAfterLine.computeIfAbsent(insertPoint, k -> new ArrayList<>()).add(patchLine);
                } else {
                    for (int j = i + 1; j < patchAddedLines.size(); j++) {
                        String neighborContent = patchAddedLines.get(j).content.trim();
                        if (!neighborContent.isEmpty() && survivingContentToLineNums.containsKey(neighborContent)) {
                            List<Integer> lineNums = survivingContentToLineNums.get(neighborContent);
                            int targetLine = lineNums.get(0);
                            int beforeLine = targetLine - 1;
                            if (beforeLine > 0) {
                                insertAfterLine.computeIfAbsent(beforeLine, k -> new ArrayList<>()).add(patchLine);
                            } else {
                                insertAtStart.add(patchLine);
                            }
                            insertPoint = -1;
                            break;
                        }
                    }
                    
                    if (insertPoint == null) {
                        insertAtEnd.add(patchLine);
                    }
                }
            }
            
            // Helper to look up user fullName from GitHub username
            java.util.function.Function<String, String> lookupFullName = (githubUsername) -> {
                if (githubUsername == null) return null;
                return githubUsernameToFullName.computeIfAbsent(githubUsername, login -> {
                    User user = userService.findByGithubUsernameOrUsername(login);
                    return user != null ? user.getFullName() : null;
                });
            };
            
            // NOTE: We skip fetching origin PR info for CURRENT lines (non-PR lines) as it's expensive
            // (would require 1 API call per unique commit SHA) and not essential for survival analysis.
            // The core metric is surviving vs deleted lines from THIS PR.
            
            // Create builder for consistent line creation
            LineDetailBuilder lineBuilder = new LineDetailBuilder(owner, repo, prFileUrl, lookupFullName);
            
            // Step 6: Build the output - current file lines with non-surviving lines interleaved
            List<PRFileDetailDTO.LineDetailDTO> resultLines = new ArrayList<>();
            
            // Add any non-surviving lines that should appear at the start
            for (PatchLineInfo nonSurviving : insertAtStart) {
                resultLines.add(lineBuilder.buildDeletedLine(
                        nonSurviving.originalLineNumber, nonSurviving.content, 
                        mergeCommitSha, prAuthorFullName, prAuthorGithubUsername));
                nonSurvivingCount++;
            }
            
            // Add current file lines with non-surviving lines inserted after appropriate positions
            for (int lineNum = 1; lineNum <= currentLines.length; lineNum++) {
                String content = currentLines[lineNum - 1];
                String commitSha = currentLineToCommit.get(lineNum);
                String authorGithub = currentLineToAuthor.get(lineNum);
                PRFileDetailDTO.LineStatus status = survivingLineNumbers.contains(lineNum) 
                        ? PRFileDetailDTO.LineStatus.SURVIVING 
                        : PRFileDetailDTO.LineStatus.CURRENT;
                
                if (status == PRFileDetailDTO.LineStatus.SURVIVING) {
                    survivingCount++;
                }
                
                // Origin PR info for CURRENT lines is not fetched to reduce API calls
                // The core survival analysis only needs to track lines from THIS PR
                resultLines.add(lineBuilder.buildCurrentLine(lineNum, content, status, commitSha, authorGithub,
                        null, null));
                
                // Insert any non-surviving lines that should appear after this line
                List<PatchLineInfo> toInsert = insertAfterLine.get(lineNum);
                if (toInsert != null) {
                    for (PatchLineInfo nonSurviving : toInsert) {
                        resultLines.add(lineBuilder.buildDeletedLine(
                                nonSurviving.originalLineNumber, nonSurviving.content,
                                mergeCommitSha, prAuthorFullName, prAuthorGithubUsername));
                        nonSurvivingCount++;
                    }
                }
            }
            
            // Add any remaining non-surviving lines at the end
            for (PatchLineInfo nonSurviving : insertAtEnd) {
                resultLines.add(lineBuilder.buildDeletedLine(
                        nonSurviving.originalLineNumber, nonSurviving.content,
                        mergeCommitSha, prAuthorFullName, prAuthorGithubUsername));
                nonSurvivingCount++;
            }
            
            fileDetail.setLines(resultLines);
            fileDetail.setSurvivingLines(survivingCount);
            fileDetail.setDeletedLines(nonSurvivingCount);
            fileDetail.setCurrentLines(currentLines.length);
            fileDetail.setPatch(null);
            
        } catch (Exception e) {
            log.debug("Error populating file line details for {}: {}", fileDetail.getFilePath(), e.getMessage());
        }
    }
    
    /**
     * Helper class to track line information from PR patch
     */
    private static class PatchLineInfo {
        final int originalLineNumber;
        final String content;
        
        PatchLineInfo(int originalLineNumber, String content) {
            this.originalLineNumber = originalLineNumber;
            this.content = content;
        }
    }
    
    /**
     * Helper class to build LineDetailDTO with all information from a single point.
     * Ensures consistent logic for all line types.
     */
    private static class LineDetailBuilder {
        private final String owner;
        private final String repo;
        private final String prFileUrl;
        private final java.util.function.Function<String, String> lookupFullName;
        
        LineDetailBuilder(String owner, String repo, String prFileUrl, 
                          java.util.function.Function<String, String> lookupFullName) {
            this.owner = owner;
            this.repo = repo;
            this.prFileUrl = prFileUrl;
            this.lookupFullName = lookupFullName;
        }
        
        private String buildCommitUrl(String commitSha) {
            if (commitSha == null) return null;
            return String.format("https://github.com/%s/%s/commit/%s", owner, repo, commitSha);
        }
        
        /**
         * Build a LineDetailDTO for a current file line (SURVIVING or CURRENT status)
         * @param originPrInfo Optional PR info for CURRENT lines (int[]{prNumber} and String prUrl)
         */
        PRFileDetailDTO.LineDetailDTO buildCurrentLine(int lineNumber, String content, 
                PRFileDetailDTO.LineStatus status, String commitSha, String authorGithubUsername,
                Integer originPrNumber, String originPrUrl) {
            String authorFullName = lookupFullName.apply(authorGithubUsername);
            String commitUrl = buildCommitUrl(commitSha);
            // For SURVIVING lines, include prFileUrl; for CURRENT lines, don't include it
            String fileUrl = status == PRFileDetailDTO.LineStatus.SURVIVING ? prFileUrl : null;
            return new PRFileDetailDTO.LineDetailDTO(
                    lineNumber, null, content, status, commitSha, 
                    commitUrl, authorFullName, authorGithubUsername, fileUrl,
                    originPrNumber, originPrUrl);
        }
        
        /**
         * Build a LineDetailDTO for a non-surviving (DELETED) line from the PR
         */
        PRFileDetailDTO.LineDetailDTO buildDeletedLine(int originalLineNumber, String content, 
                String mergeCommitSha, String prAuthorFullName, String prAuthorGithubUsername) {
            String commitUrl = buildCommitUrl(mergeCommitSha);
            return new PRFileDetailDTO.LineDetailDTO(
                    null, originalLineNumber, content, PRFileDetailDTO.LineStatus.DELETED, 
                    mergeCommitSha, commitUrl, prAuthorFullName, prAuthorGithubUsername, prFileUrl,
                    null, null);
        }
    }
}
