package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService extends BaseServiceLong<Course, CourseRepository> {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    public Course getCourse(Long id) {
        Optional<Course> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new EntityNotFound("Course does not exists");
        return oc.get();
    }

    @Transactional
    public Course createCourse(String name, String loggedInUserId) {
        User owner = userService.get(loggedInUserId);
        accessChecker.checkCanCreateCourse(owner);
        Course course = new Course(name);
        owner.addOwnCourse(course);
        course.setOwner(owner);
        return course;
    }

    public Course editCourseDetails(Long id, String name, String loggedInUserId) {
        Course course = getCourse(id);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        course.setName(name);
        repo.save(course);
        return course;
    }

    public void deleteCourse(Long id, String loggedInUserId) {
        Course course = getCourse(id);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        repo.delete(course);
    }

    List<Course> findCoursesOwned(String uuid)  {
        return this.repo.findByOwner(uuid);
    }
}
