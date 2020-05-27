package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.Iteration;
import org.udg.trackdev.spring.repository.IterationRepository;

@Service
public class IterationService extends BaseService<Iteration, IterationRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Transactional
    public Iteration create(String name, Long courseId) {
        Course course = courseService.get(courseId);
        Iteration iteration = new Iteration(name);
        course.addIteration(iteration);
        iteration.setCourse(course);
        this.repo.save(iteration);
        return iteration;
    }

}
