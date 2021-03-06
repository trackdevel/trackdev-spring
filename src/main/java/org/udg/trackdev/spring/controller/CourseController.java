package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.model.IdObjectLong;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.CourseService;
import org.udg.trackdev.spring.service.CourseYearService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/courses")
public class CourseController extends CrudController<Course, CourseService> {

    @Autowired
    CourseYearService courseYearService;

    @Autowired
    AccessChecker accessChecker;

    @GetMapping
    @JsonView(EntityLevelViews.CourseComplete.class)
    public List<Course> search(Principal principal, @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        String refinedSearch = super.scopedSearch("ownerId:"+userId, search);
        return super.search(refinedSearch);
    }

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.CourseComplete.class)
    public Course getCourse(Principal principal, @PathVariable("id") Long id) {
        Course course = service.getCourse(id);
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewCourse(course, userId);
        return course;
    }

    @PostMapping
    public IdObjectLong createCourse(Principal principal, @Valid @RequestBody NewCourse courseRequest) {
        String userId = super.getUserId(principal);
        Course createdCourse = service.createCourse(courseRequest.name, userId);

        return new IdObjectLong(createdCourse.getId());
    }

    @PutMapping(path = "/{id}")
    @JsonView(EntityLevelViews.CourseComplete.class)
    public Course editCourse(Principal principal,
                             @PathVariable("id") Long id,
                             @Valid @RequestBody EditCourse courseRequest) {
        String userId = super.getUserId(principal);
        Course modifiedCourse = service.editCourseDetails(id, courseRequest.name, userId);

        return modifiedCourse;
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCourse(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteCourse(id, userId);

        return okNoContent();
    }

    @PostMapping(path = "/{courseId}/years")
    public IdObjectLong createYear(Principal principal,
                                        @PathVariable("courseId") Long courseId,
                                        @Valid @RequestBody NewCourseYear yearRequest) {
        String userId = super.getUserId(principal);
        CourseYear createdYear = courseYearService.createCourseYear(courseId, yearRequest.startYear, userId);
        return new IdObjectLong(createdYear.getId());
    }

    @DeleteMapping(path = "/years/{yearId}")
    public ResponseEntity deleteYear(Principal principal,
                                @PathVariable("yearId") Long yearId) {
        String userId = super.getUserId(principal);
        courseYearService.deleteCourseYear(yearId, userId);
        return okNoContent();
    }

    static class NewCourse {
        @NotBlank
        @Size(max = Course.NAME_LENGTH)
        public String name;
    }

    static class EditCourse {
        @NotBlank
        @Size(max = Course.NAME_LENGTH)
        public String name;
    }

    static class NewCourseYear {
        @Min(value = 2020)
        @Max(value = 3000)
        public Integer startYear;
    }
}