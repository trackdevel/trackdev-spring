package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.ProfileRequest;
import org.trackdev.api.repository.ProfileAttributeRepository;
import org.trackdev.api.repository.ProfileEnumRepository;
import org.trackdev.api.repository.ProfileRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.*;

@Service
public class ProfileService extends BaseServiceLong<Profile, ProfileRepository> {

    @Autowired
    private UserService userService;

    @Autowired
    private AccessChecker accessChecker;

    @Autowired
    private ProfileEnumRepository enumRepository;

    @Autowired
    private ProfileAttributeRepository attributeRepository;

    public Profile getProfile(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFound(ErrorConstants.PROFILE_NOT_EXIST));
    }

    public Profile getProfile(Long id, String userId) {
        Profile profile = getProfile(id);
        accessChecker.checkCanViewProfile(profile, userId);
        return profile;
    }

    public List<Profile> getProfilesByOwner(String ownerId) {
        return repo.findByOwnerId(ownerId);
    }

    public List<Profile> getMyProfiles(String userId) {
        return repo.findByOwnerId(userId);
    }

    @Transactional
    public Profile createProfile(ProfileRequest request, String userId) {
        User owner = userService.get(userId);
        accessChecker.checkCanCreateProfile(owner);

        // Check for duplicate name
        if (repo.existsByNameAndOwnerId(request.name, userId)) {
            throw new ServiceException(ErrorConstants.PROFILE_NAME_ALREADY_EXISTS);
        }

        Profile profile = new Profile(request.name, request.description, owner);
        profile = repo.save(profile);

        // Add enums first (so attributes can reference them)
        if (request.enums != null) {
            for (ProfileRequest.EnumRequest enumRequest : request.enums) {
                addEnumToProfile(profile, enumRequest);
            }
        }

        // Add attributes
        if (request.attributes != null) {
            for (ProfileRequest.AttributeRequest attrRequest : request.attributes) {
                addAttributeToProfile(profile, attrRequest);
            }
        }

        return repo.save(profile);
    }

    @Transactional
    public Profile updateProfile(Long id, ProfileRequest request, String userId) {
        Profile profile = getProfile(id);
        accessChecker.checkCanManageProfile(profile, userId);

        // Check for duplicate name (excluding current profile)
        Optional<Profile> existing = repo.findByNameAndOwnerId(request.name, profile.getOwnerId());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ServiceException(ErrorConstants.PROFILE_NAME_ALREADY_EXISTS);
        }

        profile.setName(request.name);
        profile.setDescription(request.description);

        // Update enums
        updateEnums(profile, request.enums);

        // Update attributes
        updateAttributes(profile, request.attributes);

        return repo.save(profile);
    }

    @Transactional
    public void deleteProfile(Long id, String userId) {
        Profile profile = getProfile(id);
        accessChecker.checkCanManageProfile(profile, userId);
        repo.delete(profile);
    }

    private void addEnumToProfile(Profile profile, ProfileRequest.EnumRequest enumRequest) {
        // Check for duplicate enum name
        if (enumRepository.existsByNameAndProfileId(enumRequest.name, profile.getId())) {
            throw new ServiceException(ErrorConstants.PROFILE_ENUM_NAME_ALREADY_EXISTS);
        }

        ProfileEnum profileEnum = new ProfileEnum(enumRequest.name, profile);
        if (enumRequest.values != null) {
            profileEnum.setValues(new ArrayList<>(enumRequest.values));
        }
        profile.addEnum(profileEnum);
    }

    private void addAttributeToProfile(Profile profile, ProfileRequest.AttributeRequest attrRequest) {
        // Check for duplicate attribute name
        if (attributeRepository.existsByNameAndProfileId(attrRequest.name, profile.getId())) {
            throw new ServiceException(ErrorConstants.PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS);
        }

        ProfileAttribute attribute = new ProfileAttribute(
                attrRequest.name,
                attrRequest.type,
                attrRequest.target,
                profile
        );

        // Handle enum reference
        if (attrRequest.type == AttributeType.ENUM) {
            if (attrRequest.enumRefName == null || attrRequest.enumRefName.isBlank()) {
                throw new ServiceException(ErrorConstants.PROFILE_ENUM_REF_REQUIRED);
            }
            ProfileEnum enumRef = findEnumByName(profile, attrRequest.enumRefName);
            attribute.setEnumRef(enumRef);
        }

        profile.addAttribute(attribute);
    }

    private ProfileEnum findEnumByName(Profile profile, String enumName) {
        return profile.getEnums().stream()
                .filter(e -> e.getName().equals(enumName))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ErrorConstants.PROFILE_ENUM_REF_NOT_FOUND));
    }

    private void updateEnums(Profile profile, List<ProfileRequest.EnumRequest> enumRequests) {
        if (enumRequests == null) {
            // Remove all enums
            profile.getEnums().clear();
            return;
        }

        Set<Long> requestedIds = new HashSet<>();
        for (ProfileRequest.EnumRequest enumRequest : enumRequests) {
            if (enumRequest.id != null) {
                requestedIds.add(enumRequest.id);
                // Update existing enum
                ProfileEnum existing = profile.getEnums().stream()
                        .filter(e -> e.getId().equals(enumRequest.id))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFound(ErrorConstants.PROFILE_ENUM_REF_NOT_FOUND));
                existing.setName(enumRequest.name);
                existing.setValues(enumRequest.values != null ? new ArrayList<>(enumRequest.values) : new ArrayList<>());
            } else {
                // Add new enum
                addEnumToProfile(profile, enumRequest);
            }
        }

        // Remove enums not in request
        profile.getEnums().removeIf(e -> e.getId() != null && !requestedIds.contains(e.getId()));
    }

    private void updateAttributes(Profile profile, List<ProfileRequest.AttributeRequest> attrRequests) {
        if (attrRequests == null) {
            // Remove all attributes
            profile.getAttributes().clear();
            return;
        }

        Set<Long> requestedIds = new HashSet<>();
        for (ProfileRequest.AttributeRequest attrRequest : attrRequests) {
            if (attrRequest.id != null) {
                requestedIds.add(attrRequest.id);
                // Update existing attribute
                ProfileAttribute existing = profile.getAttributes().stream()
                        .filter(a -> a.getId().equals(attrRequest.id))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFound(ErrorConstants.ENTITY_NOT_EXIST));
                existing.setName(attrRequest.name);
                existing.setType(attrRequest.type);
                existing.setTarget(attrRequest.target);
                
                if (attrRequest.type == AttributeType.ENUM) {
                    if (attrRequest.enumRefName == null || attrRequest.enumRefName.isBlank()) {
                        throw new ServiceException(ErrorConstants.PROFILE_ENUM_REF_REQUIRED);
                    }
                    ProfileEnum enumRef = findEnumByName(profile, attrRequest.enumRefName);
                    existing.setEnumRef(enumRef);
                } else {
                    existing.setEnumRef(null);
                }
            } else {
                // Add new attribute
                addAttributeToProfile(profile, attrRequest);
            }
        }

        // Remove attributes not in request
        profile.getAttributes().removeIf(a -> a.getId() != null && !requestedIds.contains(a.getId()));
    }
}
