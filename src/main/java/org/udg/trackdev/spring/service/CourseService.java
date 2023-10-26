package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.CourseRepository;

import java.util.Collection;

@Service
public class CourseService extends BaseServiceLong<Course, CourseRepository> {

    @Autowired
    SubjectService subjectService;

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    public Collection<Course> getAll(){
        return repo.findAll();
    }

    @Transactional
    public Course createCourse(Long subjectId, Integer startYear, String loggedInUserId) {
        Subject subject = subjectService.getSubject(subjectId);
        accessChecker.checkCanManageSubject(subject, loggedInUserId);
        Course course = new Course(startYear);
        course.setSubject(subject);
        subject.addCourse(course);
        return course;
    }

    @Transactional
    public Course editCourse(Long courseId, Integer startYear, Long subjectId, String userId){
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        course.setStartYear(startYear);
        Subject subject = subjectService.getSubject(subjectId);
        course.setSubject(subject);
        repo.save(course);
        return course;
    }

    public void deleteCourse(Long courseId, String loggedInUserId) {
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        repo.delete(course);
    }

    @Transactional
    public void removeStudent(Long yearId, String username, String loggedInUserId) {
        Course course = get(yearId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        User user = userService.getByUsername(username);
        for(Project project : course.getProjects()) {
            if(project.isMember(user)) {
                project.removeMember(user);
                user.removeFromGroup(project);
            }
        }
        course.removeStudent(user);
        user.removeFromCourseYear(course);
    }

    @Transactional
    public void addStudent(Long yearId, String username, String loggedInUserId) {
        Course course = get(yearId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        User user = userService.getByUsername(username);
        if (!user.isUserType(UserType.STUDENT)){
            throw new ServiceException("User is not a student");
        }
        course.enrollStudent(user);

    }

}
