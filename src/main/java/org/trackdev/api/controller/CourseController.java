package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.CourseInvite;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.CourseInviteMapper;
import org.trackdev.api.mapper.CourseMapper;
import org.trackdev.api.mapper.ProjectMapper;
import org.trackdev.api.mapper.UserMapper;
import org.trackdev.api.service.*;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Controller for course management.
 * Courses are managed by professors and admins.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "4. Courses")
@RestController
@RequestMapping(path = "/courses")
public class CourseController extends BaseController {

    @Autowired
    CourseService service;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    CourseInviteMapper courseInviteMapper;

    @Autowired
    CourseInviteService courseInviteService;

    @Operation(summary = "Get courses", description = "Get all courses for admin, own courses for professor, or enrolled courses for student")
    @GetMapping
    public Object getCourses(Principal principal) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        if (user.isUserType(UserType.ADMIN))
            return new CoursesResponseDTO(courseMapper.toCompleteDTOList(service.getAll()));
        else if (user.isUserType(UserType.PROFESSOR))
            return new CoursesResponseDTO(courseMapper.toCompleteDTOList(service.getCoursesForUser(userId)));
        else if (user.isUserType(UserType.STUDENT)) {
            Collection<Course> courses = service.getCoursesForStudent(userId);
            Collection<CourseStudentDTO> studentDTOs = courses.stream()
                .map(course -> {
                    CourseStudentDTO dto = courseMapper.toStudentDTO(course);
                    // Filter projects to only include those where student is a member
                    Collection<ProjectBasicDTO> enrolledProjects = course.getProjects().stream()
                        .filter(project -> project.getMembers() != null && 
                                project.getMembers().stream().anyMatch(member -> member.getId().equals(userId)))
                        .map(projectMapper::toBasicDTO)
                        .collect(Collectors.toList());
                    dto.setEnrolledProjects(enrolledProjects);
                    return dto;
                })
                .collect(Collectors.toList());
            return new CoursesStudentResponseDTO(studentDTOs);
        }
        else
            throw new IllegalArgumentException(ErrorConstants.UNKNOWN_ROLE + user.getRoles());
    }

    @Operation(summary = "Get specific course", description = "Get specific course")
    @GetMapping(path = "/{courseId}")
    public CourseCompleteDTO getCourse(Principal principal, @PathVariable Long courseId) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        return courseMapper.toCompleteDTO(service.getCourse(courseId, userId));
    }

    @Operation(summary = "Get course details with students", description = "Get detailed course info including enrolled students and pending invites")
    @GetMapping(path = "/{courseId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public CourseDetailsDTO getCourseDetails(Principal principal, @PathVariable Long courseId) {
        String userId = super.getUserId(principal);
        Course course = service.getCourse(courseId, userId);
        accessChecker.checkCanManageCourse(course, userId);
        
        CourseDetailsDTO details = new CourseDetailsDTO();
        details.setId(course.getId());
        details.setStartYear(course.getStartYear());
        details.setGithubOrganization(course.getGithubOrganization());
        details.setOwnerId(course.getOwnerId());
        details.setSubject(courseMapper.toCompleteDTO(course).getSubject());
        details.setProjects(projectMapper.toWithMembersDTOList(course.getProjects()));
        details.setStudents(userMapper.toSummaryDTOList(course.getStudents()));
        
        Collection<CourseInvite> pendingInvites = courseInviteService.getPendingInvites(courseId, userId);
        details.setPendingInvites(courseInviteMapper.toDTOList(pendingInvites));
        
        return details;
    }

    @Operation(summary = "Get students enrolled in course", description = "Get all students enrolled in a specific course")
    @GetMapping(path = "/{courseId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public CourseStudentsResponseDTO getStudents(Principal principal, @PathVariable Long courseId) {
        String userId = super.getUserId(principal);
        Course course = service.getCourse(courseId, userId);
        accessChecker.checkCanManageCourse(course, userId);
        return new CourseStudentsResponseDTO(userMapper.toSummaryDTOList(course.getStudents()));
    }

    @Operation(summary = "Edit specific course", description = "Edit specific course")
    @PatchMapping(path = "/{courseId}")
    public CourseCompleteDTO editCourse(Principal principal,
                             @PathVariable Long courseId,
                             @Valid @RequestBody EditCourse courseRequest,
                             BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_COURSE_START_YEAR);
        }
        String userId = super.getUserId(principal);
        return courseMapper.toCompleteDTO(service.editCourse(courseId, courseRequest.startYear, courseRequest.subjectId,
                courseRequest.githubOrganization, courseRequest.language, userId));
    }

    @Operation(summary = "Delete specific course", description = "Delete specific course")
    @DeleteMapping(path = "/{courseId}")
    public ResponseEntity<Void> deleteCourse(Principal principal,
                                       @PathVariable Long courseId) {
        String userId = super.getUserId(principal);
        service.deleteCourse(courseId, userId);
        return okNoContent();
    }

    @Operation(summary = "Get projects enrolled to specific course", description = "Get projects enrolled to specific course")
    @GetMapping(path = "/{courseId}/projects")
    public ProjectsResponseDTO getProjects(Principal principal,
                                           @PathVariable Long courseId) {
        String userId = super.getUserId(principal);
        Course course = service.get(courseId);
        Collection<Project> projects;
        if(accessChecker.canViewCourseAllProjects(course, userId)) {
            projects = course.getProjects();
        } else {
            projects = course.getProjects().stream()
                    .filter(group -> group.isMember(userId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ProjectsResponseDTO(projectMapper.toWithMembersDTOList(projects));
    }

    @Operation(summary = "Create project enrolled to specific course", description = "Create project enrolled to specific course")
    @PostMapping(path = "/{courseId}/projects")
    public IdResponseDTO createProject(Principal principal,
                                      @PathVariable Long courseId,
                                      @Valid @RequestBody NewProject projectRequest,
                                      BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_PRJ_NAME_LENGTH);
        }
        String userId = super.getUserId(principal);
        Project createdProject = projectService.createProject(projectRequest.name, projectRequest.members, courseId, userId);
        return new IdResponseDTO(createdProject.getId());
    }


    static class NewProject {
        @NotBlank
        @Size(
                min = Project.MIN_NAME_LENGTH,
                max = Project.NAME_LENGTH
        )
        public String name;
        public Collection<String> members;
    }

    static class EditCourse {
        @Min(value = Course.MIN_START_YEAR)
        @Max(value = Course.MAX_START_YEAR)
        public Integer startYear;
        public Long subjectId;
        public String githubOrganization;
        public String language;
    }
}