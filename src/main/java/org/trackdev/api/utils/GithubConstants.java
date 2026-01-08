package org.trackdev.api.utils;

public final class GithubConstants {

    public static final String GITHUB_API_URL = "https://api.github.com";

    public static final String GITHUB_API_USER_URL = GITHUB_API_URL + "/user";

    // Repository endpoints
    public static final String GITHUB_API_REPOS_URL = GITHUB_API_URL + "/repos";
    
    // Webhook endpoints - format: /repos/{owner}/{repo}/hooks
    public static final String GITHUB_HOOKS_PATH = "/hooks";
    
    // Repository info endpoint - format: /repos/{owner}/{repo}
    public static final String getRepoUrl(String owner, String repo) {
        return GITHUB_API_REPOS_URL + "/" + owner + "/" + repo;
    }
    
    // Webhooks endpoint - format: /repos/{owner}/{repo}/hooks
    public static final String getWebhooksUrl(String owner, String repo) {
        return getRepoUrl(owner, repo) + GITHUB_HOOKS_PATH;
    }
    
    // Specific webhook endpoint - format: /repos/{owner}/{repo}/hooks/{hookId}
    public static final String getWebhookUrl(String owner, String repo, Long hookId) {
        return getWebhooksUrl(owner, repo) + "/" + hookId;
    }
    
    // Commits endpoint - format: /repos/{owner}/{repo}/commits
    public static final String getCommitsUrl(String owner, String repo) {
        return getRepoUrl(owner, repo) + "/commits";
    }
    
    // Branches endpoint - format: /repos/{owner}/{repo}/branches
    public static final String getBranchesUrl(String owner, String repo) {
        return getRepoUrl(owner, repo) + "/branches";
    }
    
    // Pull requests endpoint - format: /repos/{owner}/{repo}/pulls
    public static final String getPullsUrl(String owner, String repo) {
        return getRepoUrl(owner, repo) + "/pulls";
    }

}
