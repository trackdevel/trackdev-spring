package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for GitHub info - With token (for authorized users)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GithubInfoWithTokenDTO extends GithubInfoDTO {
    private String github_token;
}
