package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Profile;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.CourseRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.Collection;

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

}
