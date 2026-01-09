package org.trackdev.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.trackdev.api.entity.GitHubRepo;

public interface GitHubRepoRepository extends BaseRepositoryLong<GitHubRepo> {
    
    Collection<GitHubRepo> findByProjectId(Long projectId);
    
    Optional<GitHubRepo> findByProjectIdAndId(Long projectId, Long id);
    
    Optional<GitHubRepo> findByUrl(String url);
    
    boolean existsByProjectIdAndUrl(Long projectId, String url);
    
    /**
     * Find all repos matching owner and repo name (there could be multiple projects with same repo)
     */
    List<GitHubRepo> findByOwnerAndRepoName(String owner, String repoName);
}
