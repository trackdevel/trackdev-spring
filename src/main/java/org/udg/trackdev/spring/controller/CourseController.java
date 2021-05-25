package org.udg.trackdev.spring.controller;

import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.IdObjectLong;
import org.udg.trackdev.spring.service.CourseService;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/courses")
public class CourseController extends CrudController<Course, CourseService> {

    @GetMapping
    public List<Course> search(Principal principal, @RequestParam(value = "search", required = false) String search) {
        String username = principal.getName();
        String refinedSearch = "ownerId:" + username + (search != null ? "," + search : "");
        return super.search(refinedSearch);
    }

    @GetMapping(path = "/{id}")
    public Course getCourse(@PathVariable("id") Long id) {
        Course course = service.getCourse(id);
        return course;
    }

    @PostMapping
    public IdObjectLong createCourse(Principal principal, @RequestBody NewCourse courseRequest) {
        String userId = principal.getName();
        Course createdCourse = service.createCourse(courseRequest.name, userId);

        return new IdObjectLong(createdCourse.getId());
    }

    static class NewCourse {
        @NotNull
        public String name;
    }
}
