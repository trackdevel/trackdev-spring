package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.dto.ProfileAttributeDTO;
import org.trackdev.api.entity.AttributeTarget;
import org.trackdev.api.entity.AttributeType;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Profile;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.ProfileMapper;
import org.trackdev.api.repository.CourseRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService extends BaseServiceLong<Course, CourseRepository> {

    @Autowired
    SubjectService subjectService;

    @Autowired
    UserService userService;

    @Autowired
    ProfileService profileService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    ProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public Collection<Course> getAll(){
        return repo.findAllWithProjectsAndStudents();
    }

    @Transactional(readOnly = true)
    public Collection<Course> getCoursesForUser(String userId) {
        return repo.findByOwnerIdOrSubjectOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public Collection<Course> getCoursesForStudent(String userId) {
        return repo.findByStudentMembership(userId);
    }

    @Transactional(readOnly = true)
    public Collection<Course> getCoursesForWorkspace(Long workspaceId) {
        return repo.findByWorkspaceId(workspaceId);
    }

    /**
     * Get a course with authorization check.
     */
    public Course getCourse(Long courseId, String userId) {
        Course course = get(courseId);
        accessChecker.checkCanViewCourse(course, userId);
        return course;
    }

    @Transactional
    public Course createCourse(Long subjectId, Integer startYear, String organization, String loggedInUserId) {
        Subject subject = subjectService.getSubject(subjectId);
        accessChecker.checkCanCreateCourse(subject, loggedInUserId);
        
        // Check if a course already exists for this subject and year
        Course existingCourse = repo.findBySubject_IdAndStartYear(subjectId, startYear);
        if (existingCourse != null) {
            throw new ServiceException(ErrorConstants.COURSE_ALREADY_EXISTS);
        }
        
        Course course = new Course(startYear);
        course.setSubject(subject);
        course.setGithubOrganization(organization);
        course.setOwner(userService.get(loggedInUserId));
        subject.addCourse(course);
        return course;
    }

    @Transactional
    public Course editCourse(Long courseId, Integer startYear, Long subjectId, String organization, String language, String userId){
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        course.setStartYear(startYear);
        Subject subject = subjectService.getSubject(subjectId);
        course.setSubject(subject);
        course.setGithubOrganization(organization);
        if (language != null) {
            course.setLanguage(language);
        }
        repo.save(course);
        return course;
    }

    public void deleteCourse(Long courseId, String loggedInUserId) {
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        repo.delete(course);
    }

    /**
     * Apply a profile to a course, enabling custom attribute tracking for all projects in the course.
     * Only professors who manage the course can apply profiles.
     * 
     * @param courseId The ID of the course to apply the profile to
     * @param profileId The ID of the profile to apply
     * @param userId The ID of the user performing the action
     * @return The updated course with the profile applied
     */
    @Transactional
    public Course applyProfile(Long courseId, Long profileId, String userId) {
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        
        // Check if a profile has already been applied
        if (course.getProfile() != null) {
            throw new ServiceException(ErrorConstants.PROFILE_ALREADY_APPLIED);
        }
        
        Profile profile = profileService.get(profileId);
        
        // Verify the user owns the profile or is an admin
        User user = userService.get(userId);
        if (!profile.getOwnerId().equals(userId) && !user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // Set the profile reference on the course
        course.setProfile(profile);
        
        repo.save(course);
        return course;
    }

    /**
     * Get numeric TASK-targeted profile attributes for use as report magnitude.
     * Returns INTEGER and FLOAT attributes with target = TASK from the course's profile.
     * 
     * @param courseId The ID of the course
     * @param userId The ID of the user performing the action
     * @return List of profile attributes suitable for report magnitude
     */
    @Transactional(readOnly = true)
    public List<ProfileAttributeDTO> getNumericTaskAttributes(Long courseId, String userId) {
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        
        Profile profile = course.getProfile();
        if (profile == null) {
            return Collections.emptyList();
        }
        
        // Filter to only TASK-targeted numeric attributes (INTEGER or FLOAT)
        List<ProfileAttributeDTO> attributes = profile.getAttributes().stream()
            .filter(attr -> attr.getTarget() == AttributeTarget.TASK)
            .filter(attr -> attr.getType() == AttributeType.INTEGER || attr.getType() == AttributeType.FLOAT)
            .map(profileMapper::attributeToDTO)
            .collect(Collectors.toList());
        
        return attributes;
    }

}
