package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.ProfileRequest;
import org.trackdev.api.repository.*;
import org.trackdev.api.utils.ErrorConstants;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private TaskAttributeValueRepository taskAttributeValueRepository;

    @Autowired
    private StudentAttributeValueRepository studentAttributeValueRepository;

    @Autowired
    private PullRequestAttributeValueRepository pullRequestAttributeValueRepository;

    @Autowired
    private StudentAttributeListValueRepository studentAttributeListValueRepository;

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

    /**
     * Get a profile attribute by its ID.
     */
    public ProfileAttribute getAttributeById(Long attributeId) {
        return attributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFound("Attribute not found"));
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

    /**
     * Check if a profile attribute has any instantiated values across all value types.
     */
    private boolean attributeHasValues(Long attributeId) {
        return taskAttributeValueRepository.existsByAttributeId(attributeId)
                || studentAttributeValueRepository.existsByAttributeId(attributeId)
                || pullRequestAttributeValueRepository.existsByAttributeId(attributeId)
                || studentAttributeListValueRepository.existsByAttributeId(attributeId);
    }

    /**
     * Check if an enum value string is referenced by any attribute value
     * across all attribute value tables for attributes that use the given enum.
     */
    private boolean enumValueIsInUse(Profile profile, Long enumId, String value) {
        List<ProfileAttribute> referencingAttrs = profile.getAttributes().stream()
                .filter(a -> a.getEnumRef() != null && a.getEnumRef().getId().equals(enumId))
                .toList();

        for (ProfileAttribute attr : referencingAttrs) {
            Long attrId = attr.getId();
            if (attr.getType() == AttributeType.LIST) {
                if (studentAttributeListValueRepository.existsByAttributeIdAndEnumValue(attrId, value)) {
                    return true;
                }
            } else {
                if (studentAttributeValueRepository.existsByAttributeIdAndValue(attrId, value)
                        || taskAttributeValueRepository.existsByAttributeIdAndValue(attrId, value)
                        || pullRequestAttributeValueRepository.existsByAttributeIdAndValue(attrId, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validate that enum values contain no spaces.
     */
    private void validateEnumValues(List<ProfileRequest.EnumValueRequest> values) {
        if (values == null) return;
        for (ProfileRequest.EnumValueRequest entry : values) {
            if (entry.value != null && entry.value.contains(" ")) {
                throw new ServiceException(ErrorConstants.PROFILE_ENUM_VALUE_NO_SPACES);
            }
        }
    }

    private void addEnumToProfile(Profile profile, ProfileRequest.EnumRequest enumRequest) {
        // Check for duplicate enum name
        if (enumRepository.existsByNameAndProfileId(enumRequest.name, profile.getId())) {
            throw new ServiceException(ErrorConstants.PROFILE_ENUM_NAME_ALREADY_EXISTS);
        }

        // Validate no spaces in enum values
        validateEnumValues(enumRequest.values);

        ProfileEnum profileEnum = new ProfileEnum(enumRequest.name, profile);
        if (enumRequest.values != null) {
            profileEnum.setValues(enumRequest.values.stream()
                    .map(v -> new EnumValueEntry(v.value, v.description))
                    .collect(Collectors.toCollection(ArrayList::new)));
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

        // Handle LIST type constraints
        if (attrRequest.type == AttributeType.LIST) {
            if (attrRequest.target != AttributeTarget.STUDENT) {
                throw new ServiceException(ErrorConstants.LIST_ATTRIBUTE_MUST_TARGET_STUDENT);
            }
            attribute.setAppliedBy(AttributeAppliedBy.PROFESSOR);
            attribute.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
            // LIST can optionally have an enumRef for ENUM+STRING pairs
            if (attrRequest.enumRefName != null && !attrRequest.enumRefName.isBlank()) {
                ProfileEnum enumRef = findEnumByName(profile, attrRequest.enumRefName);
                attribute.setEnumRef(enumRef);
            }
            attribute.setDefaultValue(null);
            attribute.setMinValue(null);
            attribute.setMaxValue(null);
            profile.addAttribute(attribute);
            return;
        }

        // Handle enum reference
        if (attrRequest.type == AttributeType.ENUM) {
            if (attrRequest.enumRefName == null || attrRequest.enumRefName.isBlank()) {
                throw new ServiceException(ErrorConstants.PROFILE_ENUM_REF_REQUIRED);
            }
            ProfileEnum enumRef = findEnumByName(profile, attrRequest.enumRefName);
            attribute.setEnumRef(enumRef);
        }

        // Set applied by: only TASK-targeted attributes can have STUDENT appliedBy
        if (attrRequest.target != AttributeTarget.TASK) {
            attribute.setAppliedBy(AttributeAppliedBy.PROFESSOR);
        } else {
            attribute.setAppliedBy(attrRequest.appliedBy != null ? attrRequest.appliedBy : AttributeAppliedBy.PROFESSOR);
        }

        // Set visibility
        attribute.setVisibility(attrRequest.visibility != null ? attrRequest.visibility : AttributeVisibility.PROFESSOR_ONLY);

        // Set default value if provided
        attribute.setDefaultValue(attrRequest.defaultValue);

        // Set min/max values
        attribute.setMinValue(attrRequest.minValue);
        attribute.setMaxValue(attrRequest.maxValue);

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
            // Validate no enum values are in use before removing all enums
            for (ProfileEnum existingEnum : profile.getEnums()) {
                for (String val : existingEnum.getValueStrings()) {
                    if (enumValueIsInUse(profile, existingEnum.getId(), val)) {
                        ServiceException ex = new ServiceException(ErrorConstants.PROFILE_ENUM_VALUE_IN_USE);
                        ex.setMessageArgs(val);
                        throw ex;
                    }
                }
            }
            profile.getEnums().clear();
            return;
        }

        Set<Long> requestedIds = new HashSet<>();
        for (ProfileRequest.EnumRequest enumRequest : enumRequests) {
            // Validate no spaces in enum values
            validateEnumValues(enumRequest.values);

            if (enumRequest.id != null) {
                requestedIds.add(enumRequest.id);
                // Update existing enum
                ProfileEnum existing = profile.getEnums().stream()
                        .filter(e -> e.getId().equals(enumRequest.id))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFound(ErrorConstants.PROFILE_ENUM_REF_NOT_FOUND));

                // Detect removed values and validate they are not in use
                Set<String> oldValues = new HashSet<>(existing.getValueStrings());
                Set<String> newValues = enumRequest.values != null
                        ? enumRequest.values.stream().map(v -> v.value).collect(Collectors.toSet())
                        : Collections.emptySet();
                for (String removedValue : oldValues) {
                    if (!newValues.contains(removedValue)) {
                        if (enumValueIsInUse(profile, existing.getId(), removedValue)) {
                            ServiceException ex = new ServiceException(ErrorConstants.PROFILE_ENUM_VALUE_IN_USE);
                            ex.setMessageArgs(removedValue);
                            throw ex;
                        }
                    }
                }

                existing.setName(enumRequest.name);
                existing.setValues(enumRequest.values != null
                        ? enumRequest.values.stream()
                            .map(v -> new EnumValueEntry(v.value, v.description))
                            .collect(Collectors.toCollection(ArrayList::new))
                        : new ArrayList<>());
            } else {
                // Add new enum
                addEnumToProfile(profile, enumRequest);
            }
        }

        // Before removing, validate no enum values are in use
        for (ProfileEnum existingEnum : profile.getEnums()) {
            if (existingEnum.getId() != null && !requestedIds.contains(existingEnum.getId())) {
                for (String val : existingEnum.getValueStrings()) {
                    if (enumValueIsInUse(profile, existingEnum.getId(), val)) {
                        ServiceException ex = new ServiceException(ErrorConstants.PROFILE_ENUM_VALUE_IN_USE);
                        ex.setMessageArgs(val);
                        throw ex;
                    }
                }
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

                // Check immutability: if attribute has values, type/target/appliedBy/enumRef cannot change
                boolean hasValues = attributeHasValues(attrRequest.id);
                if (hasValues) {
                    if (existing.getType() != attrRequest.type) {
                        throw new ServiceException(ErrorConstants.PROFILE_ATTRIBUTE_IMMUTABLE_TYPE);
                    }
                    if (existing.getTarget() != attrRequest.target) {
                        throw new ServiceException(ErrorConstants.PROFILE_ATTRIBUTE_IMMUTABLE_TARGET);
                    }
                    AttributeAppliedBy requestedAppliedBy = attrRequest.appliedBy != null ? attrRequest.appliedBy : existing.getAppliedBy();
                    if (existing.getAppliedBy() != requestedAppliedBy) {
                        throw new ServiceException(ErrorConstants.PROFILE_ATTRIBUTE_IMMUTABLE_APPLIED_BY);
                    }
                    if (existing.getType() == AttributeType.ENUM || existing.getType() == AttributeType.LIST) {
                        String existingEnumName = existing.getEnumRef() != null ? existing.getEnumRef().getName() : null;
                        if (!Objects.equals(existingEnumName, attrRequest.enumRefName)) {
                            throw new ServiceException(ErrorConstants.PROFILE_ATTRIBUTE_IMMUTABLE_ENUM_REF);
                        }
                    }
                }

                // Update allowed fields (always allowed)
                existing.setName(attrRequest.name);
                // LIST attributes: force PROFESSOR_ONLY visibility, ignore other mutable fields
                if (existing.getType() == AttributeType.LIST || attrRequest.type == AttributeType.LIST) {
                    existing.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
                    existing.setDefaultValue(null);
                    existing.setMinValue(null);
                    existing.setMaxValue(null);
                } else {
                    existing.setDefaultValue(attrRequest.defaultValue);
                    existing.setMinValue(attrRequest.minValue);
                    existing.setMaxValue(attrRequest.maxValue);
                    if (attrRequest.visibility != null) {
                        existing.setVisibility(attrRequest.visibility);
                    }
                }

                // Update immutable fields only if no values exist
                if (!hasValues) {
                    existing.setType(attrRequest.type);
                    existing.setTarget(attrRequest.target);

                    // LIST type enforcement
                    if (attrRequest.type == AttributeType.LIST) {
                        if (attrRequest.target != AttributeTarget.STUDENT) {
                            throw new ServiceException(ErrorConstants.LIST_ATTRIBUTE_MUST_TARGET_STUDENT);
                        }
                        existing.setAppliedBy(AttributeAppliedBy.PROFESSOR);
                        existing.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
                        if (attrRequest.enumRefName != null && !attrRequest.enumRefName.isBlank()) {
                            ProfileEnum enumRef = findEnumByName(profile, attrRequest.enumRefName);
                            existing.setEnumRef(enumRef);
                        } else {
                            existing.setEnumRef(null);
                        }
                        existing.setDefaultValue(null);
                        existing.setMinValue(null);
                        existing.setMaxValue(null);
                    } else {
                        // Only TASK-targeted attributes can have STUDENT appliedBy
                        if (attrRequest.target != AttributeTarget.TASK) {
                            existing.setAppliedBy(AttributeAppliedBy.PROFESSOR);
                        } else if (attrRequest.appliedBy != null) {
                            existing.setAppliedBy(attrRequest.appliedBy);
                        }

                        if (attrRequest.type == AttributeType.ENUM) {
                            if (attrRequest.enumRefName == null || attrRequest.enumRefName.isBlank()) {
                                throw new ServiceException(ErrorConstants.PROFILE_ENUM_REF_REQUIRED);
                            }
                            ProfileEnum enumRef = findEnumByName(profile, attrRequest.enumRefName);
                            existing.setEnumRef(enumRef);
                        } else {
                            existing.setEnumRef(null);
                        }
                    }
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
