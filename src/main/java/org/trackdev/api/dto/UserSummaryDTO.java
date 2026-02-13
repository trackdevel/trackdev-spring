package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for User - Minimal info (used when embedded in tasks for project members view)
 */
@Data
public class UserSummaryDTO {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String color;
    private String capitalLetters;
    private GithubInfoDTO githubInfo;
    private DiscordInfoDTO discordInfo;
}
