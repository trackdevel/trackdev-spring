package org.trackdev.api.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class UserPushTokenDTO {
    private String id;
    private String token;
    private String platform;
    private String deviceId;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastSeenAt;
}
