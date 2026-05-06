package org.trackdev.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.trackdev.api.entity.UserPushToken;

public class RegisterPushTokenRequest {

    @NotBlank
    @Size(min = 1, max = UserPushToken.TOKEN_LENGTH)
    public String token;

    @NotNull
    public UserPushToken.Platform platform;

    @Size(max = UserPushToken.DEVICE_ID_LENGTH)
    public String deviceId;
}
