package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.PersonalAccessTokenCreatedDTO;
import org.trackdev.api.dto.PersonalAccessTokenDTO;
import org.trackdev.api.dto.PersonalAccessTokensResponseDTO;
import org.trackdev.api.entity.PersonalAccessToken;
import org.trackdev.api.model.CreatePersonalAccessTokenRequest;
import org.trackdev.api.service.PersonalAccessTokenService;
import org.trackdev.api.service.PersonalAccessTokenService.PersonalAccessTokenWithPlaintext;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "1. Authentication")
@RestController
@RequestMapping(path = "/auth/tokens")
public class PersonalAccessTokenController extends BaseController {

    @Autowired
    PersonalAccessTokenService patService;

    @Operation(summary = "Create a personal access token",
               description = "Creates a new PAT for the authenticated user. " +
                             "The full token is returned ONLY in this response.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonalAccessTokenCreatedDTO createToken(
            Principal principal,
            @Valid @RequestBody CreatePersonalAccessTokenRequest request) {
        String userId = super.getUserId(principal);
        PersonalAccessTokenWithPlaintext result = patService.createToken(
            userId, request.name, request.expiresAt);
        return toCreatedDTO(result);
    }

    @Operation(summary = "List personal access tokens",
               description = "Lists all active (non-revoked) PATs for the authenticated user.")
    @GetMapping
    public PersonalAccessTokensResponseDTO listTokens(Principal principal) {
        String userId = super.getUserId(principal);
        List<PersonalAccessToken> tokens = patService.listTokens(userId);
        List<PersonalAccessTokenDTO> dtos = tokens.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return new PersonalAccessTokensResponseDTO(dtos);
    }

    @Operation(summary = "Revoke a personal access token",
               description = "Revokes (disables) a specific PAT. This cannot be undone.")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeToken(
            Principal principal,
            @PathVariable(name = "id") String id) {
        String userId = super.getUserId(principal);
        patService.revokeToken(id, userId);
    }

    private PersonalAccessTokenDTO toDTO(PersonalAccessToken pat) {
        PersonalAccessTokenDTO dto = new PersonalAccessTokenDTO();
        dto.setId(pat.getId());
        dto.setName(pat.getName());
        dto.setTokenPrefix(pat.getTokenPrefix());
        dto.setExpiresAt(pat.getExpiresAt());
        dto.setCreatedAt(pat.getCreatedAt());
        dto.setLastUsedAt(pat.getLastUsedAt());
        dto.setRevoked(pat.getRevoked());
        return dto;
    }

    private PersonalAccessTokenCreatedDTO toCreatedDTO(PersonalAccessTokenWithPlaintext result) {
        PersonalAccessTokenCreatedDTO dto = new PersonalAccessTokenCreatedDTO();
        PersonalAccessToken pat = result.getToken();
        dto.setId(pat.getId());
        dto.setName(pat.getName());
        dto.setToken(result.getPlaintextToken());
        dto.setTokenPrefix(pat.getTokenPrefix());
        dto.setExpiresAt(pat.getExpiresAt());
        dto.setCreatedAt(pat.getCreatedAt());
        return dto;
    }
}
