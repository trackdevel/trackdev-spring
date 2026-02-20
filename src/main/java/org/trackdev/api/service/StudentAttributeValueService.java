package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.repository.StudentAttributeValueRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentAttributeValueService extends BaseServiceLong<StudentAttributeValue, StudentAttributeValueRepository> {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    public List<StudentAttributeValue> findByUserId(String userId) {
        return repo().findByUserId(userId);
    }

    public Optional<StudentAttributeValue> findByUserIdAndAttributeId(String userId, Long attributeId) {
        return repo().findByUserIdAndAttributeId(userId, attributeId);
    }

    @Transactional
    public void deleteByUserId(String userId) {
        repo().deleteByUserId(userId);
    }

    @Transactional
    public void deleteByUserIdAndAttributeId(String userId, Long attributeId) {
        repo().deleteByUserIdAndAttributeId(userId, attributeId);
    }

    /**
     * Get all attribute values for a student in a course.
     */
    public List<StudentAttributeValue> getStudentAttributeValues(Long courseId, String targetUserId, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        return repo().findByUserId(targetUserId).stream()
                .filter(v -> {
                    Profile profile = course.getProfile();
                    return profile != null && v.getAttribute().getProfileId().equals(profile.getId());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get available student-targeted attributes for a course.
     */
    public List<ProfileAttribute> getAvailableStudentAttributes(Long courseId, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);

        Profile profile = course.getProfile();
        if (profile == null) {
            return Collections.emptyList();
        }

        return profile.getAttributes().stream()
                .filter(attr -> attr.getTarget() == AttributeTarget.STUDENT)
                .collect(Collectors.toList());
    }

    /**
     * Set or update an attribute value for a student in a course.
     */
    @Transactional
    public StudentAttributeValue setStudentAttributeValue(Long courseId, String targetUserId, Long attributeId, String value, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);
        User requestingUser = userService.get(requestingUserId);

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        Profile profile = course.getProfile();
        if (profile == null) {
            throw new ServiceException("No profile is applied to this course");
        }

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);

        if (!attribute.getProfileId().equals(profile.getId())) {
            throw new ServiceException("Attribute does not belong to the course profile");
        }

        if (attribute.getTarget() != AttributeTarget.STUDENT) {
            throw new ServiceException("Attribute is not applicable to students");
        }

        checkAuthorization(attribute, requestingUser, targetUserId);
        validateAttributeValue(attribute, value);

        User targetUser = userService.get(targetUserId);
        Optional<StudentAttributeValue> existing = repo().findByUserIdAndAttributeId(targetUserId, attributeId);

        StudentAttributeValue attributeValue;
        if (existing.isPresent()) {
            attributeValue = existing.get();
            attributeValue.setValue(value);
        } else {
            attributeValue = new StudentAttributeValue(targetUser, attribute, value);
        }

        return save(attributeValue);
    }

    /**
     * Delete an attribute value for a student in a course.
     */
    @Transactional
    public void deleteStudentAttributeValue(Long courseId, String targetUserId, Long attributeId, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);
        User requestingUser = userService.get(requestingUserId);

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);
        checkAuthorization(attribute, requestingUser, targetUserId);

        repo().deleteByUserIdAndAttributeId(targetUserId, attributeId);
    }

    private void checkAuthorization(ProfileAttribute attribute, User requestingUser, String targetUserId) {
        if (attribute.getAppliedBy() == AttributeAppliedBy.PROFESSOR) {
            if (!requestingUser.isUserType(UserType.PROFESSOR) && !requestingUser.isUserType(UserType.ADMIN)) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
        } else {
            // STUDENT: the target student themselves or professor/admin
            if (requestingUser.isUserType(UserType.STUDENT)) {
                if (!requestingUser.getId().equals(targetUserId)) {
                    throw new ServiceException(ErrorConstants.UNAUTHORIZED);
                }
            } else if (!requestingUser.isUserType(UserType.PROFESSOR) && !requestingUser.isUserType(UserType.ADMIN)) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
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
