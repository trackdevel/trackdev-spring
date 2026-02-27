package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.repository.StudentAttributeListValueRepository;
import org.trackdev.api.repository.StudentAttributeValueRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing student attribute values.
 * Read access: enrolled students (filtered by visibility), professors, admins.
 * Write access: professors and admins only.
 */

@Service
public class StudentAttributeValueService extends BaseServiceLong<StudentAttributeValue, StudentAttributeValueRepository> {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AccessChecker accessChecker;

    @Autowired
    private StudentAttributeListValueRepository listValueRepository;

    /**
     * Get a course with access check that also allows enrolled students.
     * Standard checkCanViewCourse blocks students; this method adds student enrollment check.
     */
    private Course getCourseForStudentAttributeAccess(Long courseId, String requestingUserId) {
        Course course = courseService.get(courseId);
        User user = userService.get(requestingUserId);

        // Enrolled students can access student attributes (filtered by visibility later)
        if (user.isUserType(UserType.STUDENT) && course.isStudentEnrolled(requestingUserId)) {
            return course;
        }

        // For all other roles, fall back to standard course view check
        // (handles ADMIN, WORKSPACE_ADMIN, course owner, subject owner)
        accessChecker.checkCanViewCourse(course, requestingUserId);
        return course;
    }

    public List<StudentAttributeValue> findByUserId(String userId) {
        return repo().findByUserId(userId);
    }

    public Optional<StudentAttributeValue> findByUserIdAndAttributeId(String userId, Long attributeId) {
        return repo().findByUserIdAndAttributeId(userId, attributeId);
    }

    @Transactional
    public void deleteByUserId(String userId) {
        repo().deleteByUserId(userId);
        listValueRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteByUserIdAndAttributeId(String userId, Long attributeId) {
        repo().deleteByUserIdAndAttributeId(userId, attributeId);
    }

    /**
     * Get all scalar (non-LIST) attribute values for a student in a course.
     * Filters by attribute visibility based on requesting user's role.
     */
    public List<StudentAttributeValue> getStudentAttributeValues(Long courseId, String targetUserId, String requestingUserId) {
        Course course = getCourseForStudentAttributeAccess(courseId, requestingUserId);

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        User requestingUser = userService.get(requestingUserId);

        return repo().findByUserId(targetUserId).stream()
                .filter(v -> {
                    Profile profile = course.getProfile();
                    return profile != null && v.getAttribute().getProfileId().equals(profile.getId());
                })
                .filter(v -> v.getAttribute().getType() != AttributeType.LIST)
                .filter(v -> {
                    if (requestingUser.isUserType(UserType.PROFESSOR) || requestingUser.isUserType(UserType.ADMIN)) {
                        return true;
                    }
                    return isAttributeVisibleToStudent(v.getAttribute(), requestingUserId.equals(targetUserId));
                })
                .collect(Collectors.toList());
    }

    /**
     * Get available student-targeted attributes for a course.
     * Filters by visibility based on requesting user's role.
     */
    public List<ProfileAttribute> getAvailableStudentAttributes(Long courseId, String requestingUserId) {
        return getAvailableStudentAttributes(courseId, null, requestingUserId);
    }

    /**
     * Get available student-targeted attributes for a course, for a specific target student.
     * Filters by visibility based on requesting user's role.
     */
    public List<ProfileAttribute> getAvailableStudentAttributes(Long courseId, String targetUserId, String requestingUserId) {
        Course course = getCourseForStudentAttributeAccess(courseId, requestingUserId);

        Profile profile = course.getProfile();
        if (profile == null) {
            return Collections.emptyList();
        }

        User requestingUser = userService.get(requestingUserId);

        return profile.getAttributes().stream()
                .filter(attr -> attr.getTarget() == AttributeTarget.STUDENT)
                .filter(attr -> {
                    if (requestingUser.isUserType(UserType.PROFESSOR) || requestingUser.isUserType(UserType.ADMIN)) {
                        return true;
                    }
                    boolean isTargetSelf = targetUserId != null && requestingUserId.equals(targetUserId);
                    return isAttributeVisibleToStudent(attr, isTargetSelf);
                })
                .collect(Collectors.toList());
    }

    private boolean isAttributeVisibleToStudent(ProfileAttribute attr, boolean isTargetStudent) {
        return switch (attr.getVisibility()) {
            case PROFESSOR_ONLY -> false;
            case PROJECT_STUDENTS -> true;
            case ASSIGNED_STUDENT -> isTargetStudent;
        };
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

        if (attribute.getType() == AttributeType.LIST) {
            throw new ServiceException(ErrorConstants.ATTRIBUTE_NOT_LIST_TYPE);
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

    // ==================== LIST Attribute Values ====================

    /**
     * Get a LIST-type attribute with access validation.
     */
    public ProfileAttribute getListAttribute(Long courseId, Long attributeId, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);
        ProfileAttribute attribute = profileService.getAttributeById(attributeId);
        validateListAttributeAccess(attribute, course);
        return attribute;
    }

    /**
     * Get list items for a LIST-type attribute for a student in a course.
     */
    public List<StudentAttributeListValue> getStudentListAttributeValues(Long courseId, String targetUserId, Long attributeId, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);
        User requestingUser = userService.get(requestingUserId);

        // LIST attributes are PROFESSOR_ONLY visible
        if (!requestingUser.isUserType(UserType.PROFESSOR) && !requestingUser.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);
        validateListAttributeAccess(attribute, course);

        return listValueRepository.findByUserIdAndAttributeIdOrderByOrderIndex(targetUserId, attributeId);
    }

    /**
     * Replace all list items for a LIST-type attribute for a student.
     */
    @Transactional
    public List<StudentAttributeListValue> setStudentListAttributeValues(Long courseId, String targetUserId, Long attributeId,
                                                                          List<ListItemRequest> items, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);
        User requestingUser = userService.get(requestingUserId);

        if (!requestingUser.isUserType(UserType.PROFESSOR) && !requestingUser.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);
        validateListAttributeAccess(attribute, course);

        // Validate list items
        boolean hasEnumRef = attribute.getEnumRef() != null;
        List<String> allowedEnumValues = hasEnumRef ? attribute.getEnumRef().getValueStrings() : null;

        if (items != null) {
            for (ListItemRequest item : items) {
                if (hasEnumRef) {
                    if (item.enumValue == null || item.enumValue.isBlank()) {
                        throw new ServiceException(ErrorConstants.LIST_ATTRIBUTE_INVALID_ENUM_VALUE);
                    }
                    if (!allowedEnumValues.contains(item.enumValue)) {
                        throw new ServiceException(ErrorConstants.LIST_ATTRIBUTE_ENUM_VALUE_NOT_ALLOWED);
                    }
                } else {
                    if (item.enumValue != null && !item.enumValue.isBlank()) {
                        throw new ServiceException(ErrorConstants.LIST_ATTRIBUTE_ENUM_VALUE_NOT_ALLOWED);
                    }
                }
            }
        }

        // Delete existing items and flush to avoid unique constraint violation on (user_id, attribute_id, order_index)
        listValueRepository.deleteByUserIdAndAttributeId(targetUserId, attributeId);
        listValueRepository.flush();

        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        // Insert new items
        User targetUser = userService.get(targetUserId);
        List<StudentAttributeListValue> savedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ListItemRequest item = items.get(i);
            StudentAttributeListValue listValue = new StudentAttributeListValue(
                    targetUser, attribute, i,
                    hasEnumRef ? item.enumValue : null,
                    item.title,
                    item.description
            );
            savedItems.add(listValueRepository.save(listValue));
        }

        return savedItems;
    }

    /**
     * Delete all list items for a LIST-type attribute for a student.
     */
    @Transactional
    public void deleteStudentListAttributeValues(Long courseId, String targetUserId, Long attributeId, String requestingUserId) {
        Course course = courseService.getCourse(courseId, requestingUserId);
        User requestingUser = userService.get(requestingUserId);

        if (!requestingUser.isUserType(UserType.PROFESSOR) && !requestingUser.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }

        if (!course.isStudentEnrolled(targetUserId)) {
            throw new ServiceException("Student is not enrolled in this course");
        }

        ProfileAttribute attribute = profileService.getAttributeById(attributeId);
        validateListAttributeAccess(attribute, course);

        listValueRepository.deleteByUserIdAndAttributeId(targetUserId, attributeId);
    }

    private void validateListAttributeAccess(ProfileAttribute attribute, Course course) {
        Profile profile = course.getProfile();
        if (profile == null) {
            throw new ServiceException("No profile is applied to this course");
        }
        if (!attribute.getProfileId().equals(profile.getId())) {
            throw new ServiceException("Attribute does not belong to the course profile");
        }
        if (attribute.getType() != AttributeType.LIST) {
            throw new ServiceException(ErrorConstants.ATTRIBUTE_NOT_LIST_TYPE);
        }
        if (attribute.getTarget() != AttributeTarget.STUDENT) {
            throw new ServiceException("Attribute is not applicable to students");
        }
    }

    private void checkAuthorization(ProfileAttribute attribute, User requestingUser, String targetUserId) {
        // Student attributes can only be edited by PROFESSOR or ADMIN
        if (!requestingUser.isUserType(UserType.PROFESSOR) && !requestingUser.isUserType(UserType.ADMIN)) {
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

    /**
     * Request object for individual list items.
     */
    public static class ListItemRequest {
        public String enumValue;
        public String title;
        public String description;
    }
}
