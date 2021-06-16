package org.udg.trackdev.spring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.IdObjectLong;
import org.udg.trackdev.spring.service.CourseService;

import javax.validation.constraints.NotBlank;
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

    @PutMapping(path = "/{id}")
    public Course editCourse(Principal principal,
                             @PathVariable("id") Long id,
                             @RequestBody EditCourse courseRequest) {
        String userId = principal.getName();
        Course modifiedCourse = service.editCourseDetails(id, courseRequest.name, userId);

        return modifiedCourse;
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCourse(Principal principal, @PathVariable("id") Long id) {
        String userId = principal.getName();
        service.deleteCourse(id, userId);

        return ResponseEntity.ok().build();
    }

    static class NewCourse {
        @NotBlank
        public String name;
    }

    static class EditCourse {
        @NotBlank
        public String name;
    }
}
