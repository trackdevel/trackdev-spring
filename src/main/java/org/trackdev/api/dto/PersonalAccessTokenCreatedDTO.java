package org.trackdev.api.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class PersonalAccessTokenCreatedDTO {
    private String id;
    private String name;
    private String token;
    private String tokenPrefix;
    private ZonedDateTime expiresAt;
    private ZonedDateTime createdAt;
}
