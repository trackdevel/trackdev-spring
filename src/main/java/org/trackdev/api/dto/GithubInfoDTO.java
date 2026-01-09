package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for GitHub info - Basic level (excludes token)
 */
@Data
public class GithubInfoDTO {
    private String login;
    private String avatar_url;
    private String html_url;
}
