package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.model.IdObjectLong;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.CourseService;
import org.udg.trackdev.spring.service.ProjectService;
import org.udg.trackdev.spring.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/subjects/courses")
public class CourseController extends BaseController {

    @Autowired
    CourseService courseService;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @GetMapping
    @JsonView(EntityLevelViews.CourseYearComplete.class)
    public Collection<Courses> getCourses(Principal principal) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        /**NOU BLOC**/
        if (user.isUserType(UserType.ADMIN))
            return courseService.getAll();
        else if (user.isUserType(UserType.STUDENT) || user.isUserType(UserType.PROFESSOR))
            return user.getEnrolledCourseYears();
        else
            throw new IllegalArgumentException("Unknown user role: " + user.getRoles());
        /***/
        //return user.getEnrolledCourseYears();
    }

    @GetMapping(path = "/{courseId}")
    @JsonView(EntityLevelViews.CourseYearComplete.class)
    public Courses getCourse(Principal principal, @PathVariable("courseId") Long courseId) {
        String userId = super.getUserId(principal);
        Courses courses = courseService.get(courseId);
        accessChecker.checkCanViewCourseYear(courses, userId);
        return courses;
    }

    @PostMapping(path = "/{courseId}/invites")
    public IdObjectLong createInvite(Principal principal,
                                     @PathVariable("courseId") Long yearId,
                                     @Valid @RequestBody NewCourseInvite inviteRequest) {
        String userId = super.getUserId(principal);
        Invite createdInvite = courseService.createInvite(inviteRequest.email, yearId, userId);
        return new IdObjectLong(createdInvite.getId());
    }

    @GetMapping(path = "/{yearId}/students")
    @JsonView(PrivacyLevelViews.Public.class)
    public Set<User> getStudents(Principal principal,
                                 @PathVariable("yearId") Long yearId) {
        String userId = super.getUserId(principal);
        Courses courses = courseService.get(yearId);
        accessChecker.checkCanViewCourseYearAllStudents(courses, userId);
        return courses.getStudents();
    }

    @DeleteMapping(path = "/{courseId}/students/{username}")
    public ResponseEntity getStudents(Principal principal,
                                      @PathVariable("courseId") Long courseId,
                                      @PathVariable("username") String username) {
        String principalUserId = super.getUserId(principal);
        courseService.removeStudent(courseId, username, principalUserId);
        return okNoContent();
    }

    @GetMapping(path = "/{courseId}/groups")
    @JsonView(EntityLevelViews.Basic.class)
    public Collection<Project> getProjects(Principal principal,
                                           @PathVariable("courseId") Long courseId) {
        String userId = super.getUserId(principal);
        Courses courses = courseService.get(courseId);
        Collection<Project> projects;
        if(accessChecker.canViewCourseYearAllGroups(courses, userId)) {
            projects = courses.getProjects();
        } else {
            projects = courses.getProjects().stream()
                    .filter(group -> group.isMember(userId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return projects;
    }

    @PostMapping(path = "/{courseId}/groups")
    public IdObjectLong createProject(Principal principal,
                                      @PathVariable("courseId") Long courseId,
                                      @Valid @RequestBody NewProject projectRequest) {
        String userId = super.getUserId(principal);
        Project createdProject = projectService.createProject(projectRequest.name, projectRequest.members, courseId, userId);
        return new IdObjectLong(createdProject.getId());
    }

    static class NewCourseInvite {
        @NotNull
        @Email
        @Size(max = User.EMAIL_LENGTH)
        public String email;
    }

    static class NewProject {
        @NotBlank
        @Size(max = Project.NAME_LENGTH)
        public String name;

        public Collection<String> members;
    }
}