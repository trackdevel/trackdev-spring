package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.IdResponseDTO;
import org.trackdev.api.dto.ProfileCompleteDTO;
import org.trackdev.api.dto.ProfilesResponseDTO;
import org.trackdev.api.entity.Profile;
import org.trackdev.api.mapper.ProfileMapper;
import org.trackdev.api.model.ProfileRequest;
import org.trackdev.api.service.ProfileService;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for profile management.
 * Profiles can only be created, edited and deleted by professors.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "10. Profiles")
@RestController
@RequestMapping(path = "/profiles")
public class ProfileController extends CrudController<Profile, ProfileService> {

    @Autowired
    private ProfileMapper profileMapper;

    @Operation(summary = "Get my profiles", description = "Get all profiles owned by the current user")
    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ProfilesResponseDTO getMyProfiles(Principal principal) {
        String userId = super.getUserId(principal);
        List<Profile> profiles = service.getMyProfiles(userId);
        ProfilesResponseDTO response = new ProfilesResponseDTO();
        response.setProfiles(profileMapper.toBasicDTOList(profiles));
        return response;
    }

    @Operation(summary = "Get specific profile", description = "Get a profile by ID")
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ProfileCompleteDTO getProfile(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        Profile profile = service.getProfile(id, userId);
        return profileMapper.toCompleteDTO(profile);
    }

    @Operation(summary = "Create profile", description = "Create a new profile")
    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<IdResponseDTO> createProfile(
            Principal principal,
            @Valid @RequestBody ProfileRequest request,
            BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        String userId = super.getUserId(principal);
        Profile profile = service.createProfile(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdResponseDTO(profile.getId()));
    }

    @Operation(summary = "Update profile", description = "Update an existing profile")
    @PutMapping(path = "/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ProfileCompleteDTO updateProfile(
            Principal principal,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody ProfileRequest request,
            BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        String userId = super.getUserId(principal);
        Profile profile = service.updateProfile(id, request, userId);
        return profileMapper.toCompleteDTO(profile);
    }

    @Operation(summary = "Delete profile", description = "Delete a profile")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deleteProfile(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteProfile(id, userId);
        return ResponseEntity.noContent().build();
    }
}
