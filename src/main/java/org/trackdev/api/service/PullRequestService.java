package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.ActivityType;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.prchanges.PullRequestChange;
import org.trackdev.api.entity.prchanges.PullRequestClosedChange;
import org.trackdev.api.entity.prchanges.PullRequestMergedChange;
import org.trackdev.api.entity.prchanges.PullRequestOpenedChange;
import org.trackdev.api.entity.prchanges.PullRequestReopenedChange;
import org.trackdev.api.repository.PullRequestChangeRepository;
import org.trackdev.api.repository.PullRequestRepository;
import org.trackdev.api.repository.TaskRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PullRequestService extends BaseServiceUUID<PullRequest, PullRequestRepository> {

    @Autowired
    TaskService taskService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserService userService;

    @Autowired
    PullRequestChangeRepository pullRequestChangeRepository;

    @Autowired
    ActivityService activityService;

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
     * Link a pull request to a task by task key.
     * If the PR already exists (by URL), updates it. Otherwise creates a new one.
     * Records change history for tracking.
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
    @Transactional
    public PullRequest linkPullRequestToTask(String taskKey, String prUrl, String nodeId,
                                              Integer prNumber, String title, String state,
                                              Boolean merged, String repoFullName, String authorLogin,
                                              String action, String senderLogin,
                                              boolean recordChange) {
        // Find the task by key
        Task task = taskRepository.findByTaskKey(taskKey)
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
            pr.setUpdatedAt(new Date());
            
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
            pr.setCreatedAt(new Date());
            
            // Try to find author by username
            if (authorLogin != null) {
                try {
                    User author = userService.getByUsername(authorLogin);
                    pr.setAuthor(author);
                } catch (Exception e) {
                    // Author not found in system - leave as null
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
        User actor = null;
        try {
            actor = userService.getByUsername(senderLogin);
        } catch (Exception e) {
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
     */
    private void recordPullRequestChange(PullRequest pr, String action, String senderLogin,
                                          String title, Integer prNumber, String repoFullName, Boolean merged) {
        PullRequestChange change = null;
        
        // Try to find the system user who triggered this action
        User systemUser = null;
        try {
            systemUser = userService.getByUsername(senderLogin);
        } catch (Exception e) {
            // User not found in system - continue without system user reference
        }
        
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
            // Other actions (edited, synchronize) don't need change tracking
        }
        
        if (change != null) {
            pullRequestChangeRepository.save(change);
        }
    }

    /**
     * Get change history for a pull request.
     */
    public List<PullRequestChange> getPullRequestHistory(String pullRequestId) {
        PullRequest pr = get(pullRequestId);
        return pullRequestChangeRepository.findByPullRequestOrderByChangedAtDesc(pr);
    }

    /**
     * Get change history for all pull requests linked to a task.
     */
    public List<PullRequestChange> getPullRequestHistoryForTask(Long taskId) {
        Task task = taskService.get(taskId);
        List<PullRequest> prs = task.getPullRequests().stream().toList();
        return pullRequestChangeRepository.findByPullRequestInOrderByChangedAtDesc(prs);
    }

    /**
     * Generate a synthetic node ID from URL if not provided.
     */
    private String generateNodeId(String url) {
        return "PR_" + Math.abs(url.hashCode());
    }

    @Transactional
    public PullRequest findOrCreateByNodeId(String url, String nodeId, String login) {
        User author = null;
        try {
            author = userService.getByUsername(login);
        } catch (Exception e) {
            // Author not found - continue without author
        }
        
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
}
