package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.repository.PullRequestAttributeValueRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PullRequestAttributeValueService extends BaseServiceLong<PullRequestAttributeValue, PullRequestAttributeValueRepository> {

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AccessChecker accessChecker;

    public List<PullRequestAttributeValue> findByPullRequestId(String pullRequestId) {
        return repo().findByPullRequestId(pullRequestId);
    }

    public Optional<PullRequestAttributeValue> findByPullRequestIdAndAttributeId(String pullRequestId, Long attributeId) {
        return repo().findByPullRequestIdAndAttributeId(pullRequestId, attributeId);
    }

    @Transactional
    public void deleteByPullRequestId(String pullRequestId) {
        repo().deleteByPullRequestId(pullRequestId);
    }

    @Transactional
    public void deleteByPullRequestIdAndAttributeId(String pullRequestId, Long attributeId) {
        repo().deleteByPullRequestIdAndAttributeId(pullRequestId, attributeId);
    }

    /**
     * Get the course associated with a pull request by navigating PR -> tasks -> project -> course.
     */
    private Course getCourseForPullRequest(PullRequest pr) {
        for (Task task : pr.getTasks()) {
            if (task.getProject() != null && task.getProject().getCourse() != null) {
                return task.getProject().getCourse();
            }
        }
        throw new ServiceException("Pull request is not linked to any course");
    }

    /**
     * Get all attribute values for a pull request.
     * Filters by attribute visibility based on user role.
     */
    public List<PullRequestAttributeValue> getPullRequestAttributeValues(String prId, String userId) {
        PullRequest pr = pullRequestService.get(prId);
        accessChecker.checkCanViewPullRequest(pr, userId);

        Course course = getCourseForPullRequest(pr);
        Profile profile = course.getProfile();
        if (profile == null) {
            return Collections.emptyList();
        }

        User user = userService.get(userId);
        boolean isAuthor = pr.getAuthor() != null && pr.getAuthor().getId().equals(userId);

        return repo().findByPullRequestId(prId).stream()
                .filter(v -> v.getAttribute().getProfileId().equals(profile.getId()))
                .filter(v -> {
                    if (user.isUserType(UserType.PROFESSOR) || user.isUserType(UserType.ADMIN)) {
                        return true;
                    }
                    return isAttributeVisibleToStudent(v.getAttribute(), isAuthor);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get available PR-targeted attributes from the course profile.
     * Filters by visibility based on user role.
     */
    public List<ProfileAttribute> getAvailablePullRequestAttributes(String prId, String userId) {
        PullRequest pr = pullRequestService.get(prId);
        accessChecker.checkCanViewPullRequest(pr, userId);

        Course course = getCourseForPullRequest(pr);
        Profile profile = course.getProfile();
        if (profile == null) {
            return Collections.emptyList();
        }

        User user = userService.get(userId);
        boolean isAuthor = pr.getAuthor() != null && pr.getAuthor().getId().equals(userId);

        return profile.getAttributes().stream()
                .filter(attr -> attr.getTarget() == AttributeTarget.PULL_REQUEST)
                .filter(attr -> {
                    if (user.isUserType(UserType.PROFESSOR) || user.isUserType(UserType.ADMIN)) {
                        return true;
                    }
                    return isAttributeVisibleToStudent(attr, isAuthor);
                })
                .collect(Collectors.toList());
    }

    private boolean isAttributeVisibleToStudent(ProfileAttribute attr, boolean isAuthor) {
        return switch (attr.getVisibility()) {
            case PROFESSOR_ONLY -> false;
            case PROJECT_STUDENTS -> true;
            case ASSIGNED_STUDENT -> isAuthor;
        };
    }

    /**
     * Set or update an attribute value for a pull request.
     */
    @Transactional
    public PullRequestAttributeValue setPullRequestAttributeValue(String prId, Long attributeId, String value, String userId) {
        PullRequest pr = pullRequestService.get(prId);
        User user = userService.get(userId);
        accessChecker.checkCanViewPullRequest(pr, userId);

        Course course = getCourseForPullRequest(pr);
        Profile profile = course.getProfile();
        if (profile == null) {
            throw new ServiceException("No profile is applied to this course");
        }

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);

        if (!attribute.getProfileId().equals(profile.getId())) {
            throw new ServiceException("Attribute does not belong to the course profile");
        }

        if (attribute.getTarget() != AttributeTarget.PULL_REQUEST) {
            throw new ServiceException("Attribute is not applicable to pull requests");
        }

        checkAuthorization(attribute, user, pr);
        validateAttributeValue(attribute, value);

        Optional<PullRequestAttributeValue> existing = repo().findByPullRequestIdAndAttributeId(prId, attributeId);

        PullRequestAttributeValue attributeValue;
        if (existing.isPresent()) {
            attributeValue = existing.get();
            attributeValue.setValue(value);
        } else {
            attributeValue = new PullRequestAttributeValue(pr, attribute, value);
        }

        return save(attributeValue);
    }

    /**
     * Delete an attribute value from a pull request.
     */
    @Transactional
    public void deletePullRequestAttributeValue(String prId, Long attributeId, String userId) {
        PullRequest pr = pullRequestService.get(prId);
        User user = userService.get(userId);
        accessChecker.checkCanViewPullRequest(pr, userId);

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);
        checkAuthorization(attribute, user, pr);

        repo().deleteByPullRequestIdAndAttributeId(prId, attributeId);
    }

    private void checkAuthorization(ProfileAttribute attribute, User user, PullRequest pr) {
        // Pull request attributes can only be edited by PROFESSOR or ADMIN
        if (!user.isUserType(UserType.PROFESSOR) && !user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    private void validateAttributeValue(ProfileAttribute attribute, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        switch (attribute.getType()) {
            case INTEGER:
                try {
                    int intVal = Integer.parseInt(value);
                    if (attribute.getMinValue() != null && !attribute.getMinValue().isBlank()) {
                        if (intVal < Integer.parseInt(attribute.getMinValue())) {
                            throw new ServiceException(ErrorConstants.ATTRIBUTE_VALUE_BELOW_MIN);
                        }
                    }
                    if (attribute.getMaxValue() != null && !attribute.getMaxValue().isBlank()) {
                        if (intVal > Integer.parseInt(attribute.getMaxValue())) {
                            throw new ServiceException(ErrorConstants.ATTRIBUTE_VALUE_ABOVE_MAX);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new ServiceException("Invalid integer value");
                }
                break;
            case FLOAT:
                try {
                    double floatVal = Double.parseDouble(value);
                    if (attribute.getMinValue() != null && !attribute.getMinValue().isBlank()) {
                        if (floatVal < Double.parseDouble(attribute.getMinValue())) {
                            throw new ServiceException(ErrorConstants.ATTRIBUTE_VALUE_BELOW_MIN);
                        }
                    }
                    if (attribute.getMaxValue() != null && !attribute.getMaxValue().isBlank()) {
                        if (floatVal > Double.parseDouble(attribute.getMaxValue())) {
                            throw new ServiceException(ErrorConstants.ATTRIBUTE_VALUE_ABOVE_MAX);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new ServiceException("Invalid decimal value");
                }
                break;
            case ENUM:
                if (attribute.getEnumRef() != null) {
                    List<String> allowedValues = attribute.getEnumRef().getValueStrings();
                    if (!allowedValues.contains(value)) {
                        throw new ServiceException("Invalid enum value. Allowed values: " + String.join(", ", allowedValues));
                    }
                }
                break;
            case STRING:
            default:
                break;
        }
    }
}
