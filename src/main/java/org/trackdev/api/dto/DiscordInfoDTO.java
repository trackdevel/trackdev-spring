package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for Discord info - Basic level (excludes tokens)
 */
@Data
public class DiscordInfoDTO {
    private String discordId;
    private String username;
    private String discriminator;
    private String avatarHash;
}
