package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Subject;
import org.udg.trackdev.spring.entity.Courses;
import org.udg.trackdev.spring.model.IdObjectLong;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.SubjectService;
import org.udg.trackdev.spring.service.CourseYearService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/subjects")
public class SubjectController extends CrudController<Subject, SubjectService> {

    @Autowired
    CourseYearService courseYearService;

    @Autowired
    AccessChecker accessChecker;

    @GetMapping
    @JsonView(EntityLevelViews.CourseComplete.class)
    public List<Subject> search(Principal principal, @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        String refinedSearch = super.scopedSearch("ownerId:"+userId, search);
        return super.search(refinedSearch);
    }

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.CourseComplete.class)
    public Subject getSubject(Principal principal, @PathVariable("id") Long id) {
        Subject subject = service.getCourse(id);
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewCourse(subject, userId);
        return subject;
    }

    @PostMapping
    public IdObjectLong createSubject(Principal principal, @Valid @RequestBody NewCourse courseRequest) {
        String userId = super.getUserId(principal);
        Subject createdSubject = service.createCourse(courseRequest.name, courseRequest.acronym, userId);

        return new IdObjectLong(createdSubject.getId());
    }

    @PutMapping(path = "/{id}")
    @JsonView(EntityLevelViews.CourseComplete.class)
    public Subject editSubject(Principal principal,
                               @PathVariable("id") Long id,
                               @Valid @RequestBody EditCourse courseRequest) {
        String userId = super.getUserId(principal);
        Subject modifiedSubject = service.editCourseDetails(id, courseRequest.name, userId);

        return modifiedSubject;
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteSubject(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteCourse(id, userId);

        return okNoContent();
    }

    @PostMapping(path = "/{courseId}/years")
    public IdObjectLong createYear(Principal principal,
                                        @PathVariable("courseId") Long courseId,
                                        @Valid @RequestBody NewCourseYear yearRequest) {
        String userId = super.getUserId(principal);
        Courses createdYear = courseYearService.createCourseYear(courseId, yearRequest.startYear, userId);
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
        @Size(max = Subject.NAME_LENGTH)
        public String name;
        public String acronym;
    }

    static class EditCourse {
        @NotBlank
        @Size(max = Subject.NAME_LENGTH)
        public String name;
    }

    static class NewCourseYear {
        @Min(value = 2020)
        @Max(value = 3000)
        public Integer startYear;
    }
}