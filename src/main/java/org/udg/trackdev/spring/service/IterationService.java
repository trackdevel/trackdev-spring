package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.entity.Iteration;
import org.udg.trackdev.spring.repository.IterationRepository;

@Service
public class IterationService extends BaseServiceLong<Iteration, IterationRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseYearService courseYearService;

    @Transactional
    public Iteration create(String name, Long courseYearId) {
        CourseYear courseYear = courseYearService.get(courseYearId);
        Iteration iteration = new Iteration(name);
        courseYear.addIteration(iteration);
        iteration.setCourseYear(courseYear);
        this.repo.save(iteration);
        return iteration;
    }

}
