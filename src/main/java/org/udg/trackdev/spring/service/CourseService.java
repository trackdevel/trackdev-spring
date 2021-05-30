package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService extends BaseService<Course, CourseRepository> {

    @Autowired
    UserService userService;

    public Course getCourse(Long id) {
        Optional<Course> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new ServiceException("Course does not exists");
        return oc.get();
    }

    @Transactional
    public Course createCourse(String name, String ownerId) {
        User owner = userService.get(ownerId);
        boolean isProfessor = owner.getRoles().stream()
                .anyMatch(r -> r.getUserType() == UserType.PROFESSOR);
        if(!isProfessor) {
            throw new ServiceException("User does not have rights to create courses");
        }
        Course course = new Course(name);
        owner.addOwnCourse(course);
        course.setOwner(owner);
        return course;
    }

    List<Course> findCoursesOwned(String uuid)  {
        return this.repo.findByOwner(uuid);
    }

}
