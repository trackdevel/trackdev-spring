package org.trackdev.api.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class PersonalAccessTokenDTO {
    private String id;
    private String name;
    private String tokenPrefix;
    private ZonedDateTime expiresAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastUsedAt;
    private Boolean revoked;
}
