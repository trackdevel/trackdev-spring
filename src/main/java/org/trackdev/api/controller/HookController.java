package org.trackdev.api.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.PullRequestDTO;
import org.trackdev.api.entity.GitHubRepo;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.mapper.PullRequestMapper;
import org.trackdev.api.repository.GitHubRepoRepository;
import org.trackdev.api.service.PullRequestService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for handling GitHub webhooks.
 * Receives PR events and links PRs to tasks based on task keys in PR description.
 * 
 * Security: Webhooks are authenticated via HMAC-SHA256 signature verification.
 * Each repository has its own unique webhook secret stored in the database.
 */
@Hidden
@RestController
@RequestMapping(path = "/hooks")
@ResponseBody
public class HookController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(HookController.class);
    
    /**
     * Pattern to match task keys in PR description.
     * Matches patterns like: abc-1, ABC-123, a7k-42
     */
    private static final Pattern TASK_KEY_PATTERN = Pattern.compile("[A-Za-z0-9]{3,5}-[1-9][0-9]{0,3}");

    @Autowired
    PullRequestService pullRequestService;

    @Autowired
    GitHubRepoRepository gitHubRepoRepository;

    @Autowired
    PullRequestMapper pullRequestMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle GitHub pull_request webhook events.
     * 
     * Security: Verifies the X-Hub-Signature-256 header against the stored
     * webhook secret for the repository. Returns 401 if signature is invalid.
     */
    @PostMapping(path = "/github/pr")
    public ResponseEntity<WebhookResponse> handlePullRequest(
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(name = "X-GitHub-Event", required = false) String event,
            @RequestHeader(name = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestBody String rawPayload) {
        
        log.info("Received GitHub webhook: event={}, delivery={}", event, deliveryId);
        
        // Only process pull_request events
        if (!"pull_request".equals(event)) {
            log.debug("Ignoring non-pull_request event: {}", event);
            return ResponseEntity.ok(new WebhookResponse("ignored", "Not a pull_request event"));
        }

        // Parse the payload
        GithubPullRequestEvent prEvent;
        try {
            prEvent = objectMapper.readValue(rawPayload, GithubPullRequestEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            return ResponseEntity.badRequest().body(new WebhookResponse("error", "Invalid payload"));
        }

        // Get repository info for signature verification
        String repoFullName = prEvent.repository != null ? prEvent.repository.full_name : null;
        if (repoFullName == null) {
            log.warn("Webhook missing repository information");
            return ResponseEntity.badRequest().body(new WebhookResponse("error", "Missing repository info"));
        }

        // Parse owner/repo from full_name (format: "owner/repo")
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            log.warn("Invalid repository full_name format: {}", repoFullName);
            return ResponseEntity.badRequest().body(new WebhookResponse("error", "Invalid repository format"));
        }
        String owner = parts[0];
        String repoName = parts[1];

        // Find the repository in our database
        List<GitHubRepo> repos = gitHubRepoRepository.findByOwnerAndRepoName(owner, repoName);
        if (repos.isEmpty()) {
            log.warn("Repository not found in database: {}", repoFullName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new WebhookResponse("error", "Repository not registered"));
        }

        // Verify signature against at least one matching repo's secret
        boolean signatureValid = false;
        for (GitHubRepo repo : repos) {
            String secret = repo.getWebhookSecret();
            if (secret != null && !secret.isEmpty() && verifySignature(rawPayload, signature, secret)) {
                signatureValid = true;
                log.debug("Signature verified for repo {} (project {})", repoFullName, repo.getProjectId());
                break;
            }
        }

        if (!signatureValid) {
            log.warn("Invalid webhook signature for repository: {}", repoFullName);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new WebhookResponse("error", "Invalid signature"));
        }

        // Only process opened, edited, reopened, and synchronize actions
        String action = prEvent.action;
        if (!isRelevantAction(action)) {
            log.debug("Ignoring action: {}", action);
            return ResponseEntity.ok(new WebhookResponse("ignored", "Action not relevant: " + action));
        }
        
        GithubPR pr = prEvent.pull_request;
        if (pr == null) {
            log.warn("Received pull_request event without pull_request data");
            return ResponseEntity.badRequest().body(new WebhookResponse("error", "Missing pull_request data"));
        }
        
        String authorLogin = pr.user != null ? pr.user.login : null;
        String senderLogin = prEvent.sender != null ? prEvent.sender.login : null;
        
        log.info("Processing PR #{} from {} - action: {} by {}", pr.number, repoFullName, action, senderLogin);
        
        // Always create/update the PR and record the change event first
        PullRequest pullRequest = pullRequestService.processWebhookEvent(
                pr.html_url,
                pr.node_id,
                pr.number,
                pr.title,
                pr.body,
                pr.state,
                pr.merged,
                repoFullName,
                authorLogin,
                action,
                senderLogin
        );
        
        // Extract task keys from PR body for linking
        String prBody = pr.body != null ? pr.body : "";
        Set<String> taskKeys = extractTaskKeys(prBody);
        
        if (taskKeys.isEmpty()) {
            log.debug("No task keys found in PR #{}", pr.number);
            return ResponseEntity.ok(new WebhookResponse("ok", "No task keys found in PR description"));
        }
        
        log.info("Found task keys in PR #{}: {}", pr.number, taskKeys);
        
        // Link PR to each matching task (PR already created, just need to link)
        List<PullRequestDTO> linkedPRs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (String taskKey : taskKeys) {
            try {
                pullRequestService.linkPullRequestToTask(taskKey.toLowerCase(), pullRequest, action, senderLogin);
                linkedPRs.add(pullRequestMapper.toDTO(pullRequest));
                log.info("Linked PR #{} to task {}", pr.number, taskKey);
            } catch (Exception e) {
                log.warn("Failed to link PR #{} to task {}: {}", pr.number, taskKey, e.getMessage());
                errors.add(taskKey + ": " + e.getMessage());
            }
        }
        
        WebhookResponse response = new WebhookResponse(
                errors.isEmpty() ? "ok" : "partial",
                String.format("Linked to %d tasks", linkedPRs.size())
        );
        response.linkedTasks = taskKeys.stream().toList();
        response.errors = errors.isEmpty() ? null : errors;
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify GitHub webhook signature using HMAC-SHA256.
     * 
     * @param payload The raw request body
     * @param signature The X-Hub-Signature-256 header value
     * @param secret The webhook secret stored for this repository
     * @return true if signature is valid
     */
    private boolean verifySignature(String payload, String signature, String secret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            log.debug("Missing or invalid signature format");
            return false;
        }
        
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            String expectedSignature = "sha256=" + bytesToHex(hash);
            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));

            if (!valid) {
                log.debug("Signature mismatch for webhook");
            }

            return valid;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to verify webhook signature", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Check if the PR action should trigger task linking/updating.
     * Includes "closed" to handle merged PRs.
     */
    private boolean isRelevantAction(String action) {
        return "opened".equals(action) || 
               "edited".equals(action) || 
               "reopened".equals(action) ||
               "synchronize".equals(action) ||
               "closed".equals(action);
    }

    /**
     * Extract all task keys from text.
     * Task keys match the pattern: [A-Za-z0-9]{3,5}-[1-9][0-9]{0,3}
     */
    private Set<String> extractTaskKeys(String text) {
        Set<String> taskKeys = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) {
            return taskKeys;
        }
        
        Matcher matcher = TASK_KEY_PATTERN.matcher(text);
        while (matcher.find()) {
            taskKeys.add(matcher.group());
        }
        
        return taskKeys;
    }

    // ========== REQUEST/RESPONSE DTOs ==========

    static class WebhookResponse {
        public String status;
        public String message;
        public List<String> linkedTasks;
        public List<String> errors;

        public WebhookResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    // GitHub webhook payload DTOs
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubPullRequestEvent {
        public String action;
        public Long number;
        public GithubPR pull_request;
        public GithubUser sender;
        public GithubRepo repository;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubPR {
        public Long id;
        public String node_id;
        public Integer number;
        public String url;
        public String html_url;
        public String title;
        public String body;
        public String state;
        public Boolean merged;
        public GithubUser user;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubUser {
        public Long id;
        public String login;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubRepo {
        public Long id;
        public String full_name;
    }
}
