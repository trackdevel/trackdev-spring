package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.Subject;
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
    public Course createCourse(Long subjectId, Integer startYear, String organization, String loggedInUserId) {
        Subject subject = subjectService.getSubject(subjectId);
        accessChecker.checkCanManageSubject(subject, loggedInUserId);
        Course course = new Course(startYear);
        course.setSubject(subject);
        course.setGithubOrganization(organization);
        subject.addCourse(course);
        return course;
    }

    @Transactional
    public Course editCourse(Long courseId, Integer startYear, Long subjectId, String organization, String userId){
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, userId);
        course.setStartYear(startYear);
        Subject subject = subjectService.getSubject(subjectId);
        course.setSubject(subject);
        course.setGithubOrganization(organization);
        repo.save(course);
        return course;
    }

    public void deleteCourse(Long courseId, String loggedInUserId) {
        Course course = get(courseId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        repo.delete(course);
    }

}
