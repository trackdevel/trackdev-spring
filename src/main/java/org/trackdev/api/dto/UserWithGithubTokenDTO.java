package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * DTO for User - With GitHub token (for current user self view)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserWithGithubTokenDTO extends UserBasicDTO {
    private Collection<ProjectBasicDTO> projects;
    private GithubInfoWithTokenDTO githubInfo;
}
