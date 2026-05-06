package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.UserPushTokenDTO;
import org.trackdev.api.dto.UserPushTokensResponseDTO;
import org.trackdev.api.entity.UserPushToken;
import org.trackdev.api.model.RegisterPushTokenRequest;
import org.trackdev.api.service.UserPushTokenService;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "1. Authentication")
@RestController
@RequestMapping(path = "/users/me/push-tokens")
@PreAuthorize("isAuthenticated()")
public class UserPushTokenController extends BaseController {

    @Autowired
    UserPushTokenService pushTokenService;

    @Operation(summary = "Register a push notification token",
               description = "Registers (or refreshes) a push notification token for the " +
                             "authenticated user's device. Idempotent on token: if the same " +
                             "token already exists, its owner, platform, deviceId and " +
                             "lastSeenAt are updated.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserPushTokenDTO registerToken(
            Principal principal,
            @Valid @RequestBody RegisterPushTokenRequest request) {
        String userId = super.getUserId(principal);
        UserPushToken pt = pushTokenService.registerToken(
            userId, request.token, request.platform, request.deviceId);
        return toDTO(pt);
    }

    @Operation(summary = "List push notification tokens",
               description = "Lists all push notification tokens registered for the " +
                             "authenticated user.")
    @GetMapping
    public UserPushTokensResponseDTO listTokens(Principal principal) {
        String userId = super.getUserId(principal);
        List<UserPushToken> tokens = pushTokenService.listTokens(userId);
        List<UserPushTokenDTO> dtos = tokens.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return new UserPushTokensResponseDTO(dtos);
    }

    @Operation(summary = "Unregister a push notification token",
               description = "Removes a push notification token for the authenticated user. " +
                             "Returns 204 even if the token does not exist (idempotent).")
    @DeleteMapping(path = "/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregisterToken(
            Principal principal,
            @PathVariable(name = "token") String token) {
        String userId = super.getUserId(principal);
        pushTokenService.unregisterToken(userId, token);
    }

    private UserPushTokenDTO toDTO(UserPushToken pt) {
        UserPushTokenDTO dto = new UserPushTokenDTO();
        dto.setId(pt.getId());
        dto.setToken(pt.getToken());
        dto.setPlatform(pt.getPlatform().name());
        dto.setDeviceId(pt.getDeviceId());
        dto.setCreatedAt(pt.getCreatedAt());
        dto.setLastSeenAt(pt.getLastSeenAt());
        return dto;
    }
}
