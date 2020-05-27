package org.udg.trackdev.spring.controller;

import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.service.CourseService;

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

}
