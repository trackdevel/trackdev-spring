package org.trackdev.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;

public class CreatePersonalAccessTokenRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    public String name;

    public ZonedDateTime expiresAt;
}
