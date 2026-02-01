package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.UserMapper;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that provides privacy-aware user DTO mapping.
 * Applies email filtering based on the current user's permissions.
 */
@Service
public class PrivacyAwareUserMapper {

    private final UserMapper userMapper;
    private final PrivacyService privacyService;

    public PrivacyAwareUserMapper(UserMapper userMapper, PrivacyService privacyService) {
        this.userMapper = userMapper;
        this.privacyService = privacyService;
    }

    /**
     * Map user to BasicDTO with privacy filtering applied.
     */
    public UserBasicDTO toBasicDTO(User user) {
        UserBasicDTO dto = userMapper.toBasicDTO(user);
        applyPrivacyFilter(dto, user.getId());
        return dto;
    }

    /**
     * Map user to SummaryDTO with privacy filtering applied.
     */
    public UserSummaryDTO toSummaryDTO(User user) {
        UserSummaryDTO dto = userMapper.toSummaryDTO(user);
        applyPrivacyFilter(dto, user.getId());
        return dto;
    }

    /**
     * Map user to WithProjectsDTO with privacy filtering applied.
     */
    public UserWithProjectsDTO toWithProjectsDTO(User user) {
        UserWithProjectsDTO dto = userMapper.toWithProjectsDTO(user);
        applyPrivacyFilter(dto, user.getId());
        return dto;
    }

    /**
     * Map user to WithGithubTokenDTO with privacy filtering applied.
     * Note: This is typically used for the current user only.
     */
    public UserWithGithubTokenDTO toWithGithubTokenDTO(User user) {
        UserWithGithubTokenDTO dto = userMapper.toWithGithubTokenDTO(user);
        applyPrivacyFilter(dto, user.getId());
        return dto;
    }

    /**
     * Map collection of users to WithProjectsDTOs with privacy filtering.
     */
    public Collection<UserWithProjectsDTO> toWithProjectsDTOList(Collection<User> users) {
        return users.stream()
                .map(this::toWithProjectsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map collection of users to SummaryDTOs with privacy filtering.
     */
    public Collection<UserSummaryDTO> toSummaryDTOList(Collection<User> users) {
        return users.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map set of users to SummaryDTOs with privacy filtering.
     */
    public Set<UserSummaryDTO> toSummaryDTOSet(Set<User> users) {
        return users.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toSet());
    }

    /**
     * Apply privacy filter to UserBasicDTO - hide email if not allowed.
     */
    private void applyPrivacyFilter(UserBasicDTO dto, String userId) {
        if (!privacyService.canSeeEmail(userId)) {
            dto.setEmail(null);
        }
    }

    /**
     * Apply privacy filter to UserSummaryDTO - hide email if not allowed.
     */
    private void applyPrivacyFilter(UserSummaryDTO dto, String userId) {
        if (!privacyService.canSeeEmail(userId)) {
            dto.setEmail(null);
        }
    }

    /**
     * Apply privacy filter to UserWithProjectsDTO - hide email if not allowed.
     */
    private void applyPrivacyFilter(UserWithProjectsDTO dto, String userId) {
        if (!privacyService.canSeeEmail(userId)) {
            dto.setEmail(null);
        }
    }

    /**
     * Apply privacy filter to UserWithGithubTokenDTO - hide email if not allowed.
     */
    private void applyPrivacyFilter(UserWithGithubTokenDTO dto, String userId) {
        if (!privacyService.canSeeEmail(userId)) {
            dto.setEmail(null);
        }
    }
}
