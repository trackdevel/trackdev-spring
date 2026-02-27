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
import org.trackdev.api.entity.Report;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.ProfileAttribute;
import org.trackdev.api.entity.StudentAttributeListValue;
import org.trackdev.api.entity.StudentAttributeValue;
import org.trackdev.api.mapper.CourseInviteMapper;
import org.trackdev.api.mapper.CourseMapper;
import org.trackdev.api.mapper.ProfileMapper;
import org.trackdev.api.mapper.ProjectMapper;
import org.trackdev.api.mapper.ReportMapper;
import org.trackdev.api.mapper.StudentAttributeValueMapper;
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
import java.util.List;
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

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    ReportService reportService;

    @Autowired
    StudentAttributeValueService studentAttributeValueService;

    @Autowired
    StudentAttributeValueMapper studentAttributeValueMapper;

    @Autowired
    ProfileMapper profileMapper;

    @Operation(summary = "Get courses", description = "Get all courses for admin/workspace admin, own courses for professor, or enrolled courses for student")
    @GetMapping
    public Object getCourses(Principal principal) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        if (user.isUserType(UserType.ADMIN))
            return new CoursesResponseDTO(courseMapper.toCompleteDTOList(service.getAll()));
        else if (user.isUserType(UserType.WORKSPACE_ADMIN)) {
            // WORKSPACE_ADMIN can only see courses from their workspace
            if (user.getWorkspace() == null) {
                return new CoursesResponseDTO(courseMapper.toCompleteDTOList(List.of()));
            }
            return new CoursesResponseDTO(courseMapper.toCompleteDTOList(service.getCoursesForWorkspace(user.getWorkspace().getId())));
        }
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
    public CourseCompleteDTO getCourse(Principal principal, @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        return courseMapper.toCompleteDTO(service.getCourse(courseId, userId));
    }

    @Operation(summary = "Get course details with students", description = "Get detailed course info including enrolled students and pending invites")
    @GetMapping(path = "/{courseId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public CourseDetailsDTO getCourseDetails(Principal principal, @PathVariable(name = "courseId") Long courseId) {
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
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public CourseStudentsResponseDTO getStudents(Principal principal, @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        Course course = service.getCourse(courseId, userId);
        accessChecker.checkCanManageCourse(course, userId);
        return new CourseStudentsResponseDTO(userMapper.toSummaryDTOList(course.getStudents()));
    }

    @Operation(summary = "Edit specific course", description = "Edit specific course")
    @PatchMapping(path = "/{courseId}")
    public CourseCompleteDTO editCourse(Principal principal,
                             @PathVariable(name = "courseId") Long courseId,
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
                                       @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        service.deleteCourse(courseId, userId);
        return okNoContent();
    }

    @Operation(summary = "Get projects enrolled to specific course", description = "Get projects enrolled to specific course")
    @GetMapping(path = "/{courseId}/projects")
    public ProjectsResponseDTO getProjects(Principal principal,
                                           @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        Course course = service.get(courseId);
        Collection<Project> projects;
        if(accessChecker.canViewCourseAllProjects(course, userId)) {
            projects = course.getProjects();
        } else {
            // Students must be enrolled in the course to see any projects
            if (!course.isStudentEnrolled(userId)) {
                throw new ControllerException(ErrorConstants.UNAUTHORIZED);
            }
            projects = course.getProjects().stream()
                    .filter(group -> group.isMember(userId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        // Sort projects alphabetically by name
        List<Project> sortedProjects = projects.stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
        return new ProjectsResponseDTO(projectMapper.toWithMembersDTOList(sortedProjects));
    }

    @Operation(summary = "Create project enrolled to specific course", description = "Create project enrolled to specific course")
    @PostMapping(path = "/{courseId}/projects")
    public IdResponseDTO createProject(Principal principal,
                                      @PathVariable(name = "courseId") Long courseId,
                                      @Valid @RequestBody NewProject projectRequest,
                                      BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_PRJ_NAME_LENGTH);
        }
        String userId = super.getUserId(principal);
        Project createdProject = projectService.createProject(projectRequest.name, projectRequest.members, courseId, userId);
        return new IdResponseDTO(createdProject.getId());
    }

    @Operation(summary = "Get reports assigned to a course", description = "Get all reports that have been assigned to this course by professors")
    @GetMapping(path = "/{courseId}/reports")
    public CourseReportsResponse getCourseReports(Principal principal,
                                                   @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        List<Report> reports = reportService.getReportsForCourse(courseId, userId);
        return new CourseReportsResponse(reportMapper.toBasicDTOList(reports), courseId);
    }

    @Operation(summary = "Get numeric profile attributes for report magnitude", 
               description = "Get TASK-targeted INTEGER and FLOAT attributes from the course's profile for use as report magnitude")
    @GetMapping(path = "/{courseId}/report-magnitude-attributes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public List<ProfileAttributeDTO> getReportMagnitudeAttributes(Principal principal,
                                                                    @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        return service.getNumericTaskAttributes(courseId, userId);
    }

    @Operation(summary = "Apply a profile to a course", description = "Apply a profile to a course, enabling custom attribute tracking for all projects (professors only)")
    @PostMapping(path = "/{courseId}/apply-profile/{profileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public CourseCompleteDTO applyProfile(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "profileId") Long profileId) {
        String userId = super.getUserId(principal);
        Course course = service.applyProfile(courseId, profileId, userId);
        return courseMapper.toCompleteDTO(course);
    }


    // ==================== Student Attribute Values ====================

    @Operation(summary = "Get attribute values for a student in a course", description = "Get all attribute values set for a student in this course")
    @GetMapping(path = "/{courseId}/students/{userId}/attributes")
    public List<StudentAttributeValueDTO> getStudentAttributeValues(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "userId") String userId) {
        String requestingUserId = super.getUserId(principal);
        List<StudentAttributeValue> values = studentAttributeValueService.getStudentAttributeValues(courseId, userId, requestingUserId);
        return studentAttributeValueMapper.toDTOList(values);
    }

    @Operation(summary = "Get available student attributes for a course", description = "Get attributes from course profile that can be applied to students")
    @GetMapping(path = "/{courseId}/student-attributes")
    public List<ProfileAttributeDTO> getAvailableStudentAttributes(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId) {
        String requestingUserId = super.getUserId(principal);
        List<ProfileAttribute> attributes = studentAttributeValueService.getAvailableStudentAttributes(courseId, requestingUserId);
        return profileMapper.attributesToDTO(attributes);
    }

    @Operation(summary = "Set attribute value for a student", description = "Set or update an attribute value for a student (professors only).")
    @PutMapping(path = "/{courseId}/students/{userId}/attributes/{attributeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public StudentAttributeValueDTO setStudentAttributeValue(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "attributeId") Long attributeId,
            @RequestBody SetAttributeValueRequest request) {
        String requestingUserId = super.getUserId(principal);
        StudentAttributeValue value = studentAttributeValueService.setStudentAttributeValue(courseId, userId, attributeId, request.value, requestingUserId);
        return studentAttributeValueMapper.toDTO(value);
    }

    @Operation(summary = "Delete attribute value from a student", description = "Remove an attribute value from a student (professors only).")
    @DeleteMapping(path = "/{courseId}/students/{userId}/attributes/{attributeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteStudentAttributeValue(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "attributeId") Long attributeId) {
        String requestingUserId = super.getUserId(principal);
        studentAttributeValueService.deleteStudentAttributeValue(courseId, userId, attributeId, requestingUserId);
    }

    // ==================== Student LIST Attribute Values ====================

    @Operation(summary = "Get list attribute values for a student", description = "Get all items for a LIST-type attribute for a student (professors only)")
    @GetMapping(path = "/{courseId}/students/{userId}/list-attributes/{attributeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public StudentAttributeListValueDTO getStudentListAttributeValues(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "attributeId") Long attributeId) {
        String requestingUserId = super.getUserId(principal);
        ProfileAttribute attribute = studentAttributeValueService.getListAttribute(courseId, attributeId, requestingUserId);
        List<StudentAttributeListValue> items = studentAttributeValueService.getStudentListAttributeValues(courseId, userId, attributeId, requestingUserId);
        return studentAttributeValueMapper.toListValueDTO(attribute, items);
    }

    @Operation(summary = "Set list attribute values for a student", description = "Replace all items for a LIST-type attribute for a student (professors only)")
    @PutMapping(path = "/{courseId}/students/{userId}/list-attributes/{attributeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public StudentAttributeListValueDTO setStudentListAttributeValues(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "attributeId") Long attributeId,
            @RequestBody SetListAttributeValuesRequest request) {
        String requestingUserId = super.getUserId(principal);
        List<StudentAttributeValueService.ListItemRequest> items = null;
        if (request.items != null) {
            items = request.items.stream().map(item -> {
                StudentAttributeValueService.ListItemRequest r = new StudentAttributeValueService.ListItemRequest();
                r.enumValue = item.enumValue;
                r.title = item.title;
                r.description = item.description;
                return r;
            }).collect(Collectors.toList());
        }
        List<StudentAttributeListValue> savedItems = studentAttributeValueService.setStudentListAttributeValues(courseId, userId, attributeId, items, requestingUserId);
        ProfileAttribute attribute = studentAttributeValueService.getListAttribute(courseId, attributeId, requestingUserId);
        return studentAttributeValueMapper.toListValueDTO(attribute, savedItems);
    }

    @Operation(summary = "Delete list attribute values for a student", description = "Remove all items for a LIST-type attribute from a student (professors only)")
    @DeleteMapping(path = "/{courseId}/students/{userId}/list-attributes/{attributeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteStudentListAttributeValues(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "attributeId") Long attributeId) {
        String requestingUserId = super.getUserId(principal);
        studentAttributeValueService.deleteStudentListAttributeValues(courseId, userId, attributeId, requestingUserId);
    }

    static class SetAttributeValueRequest {
        public String value;
    }

    static class SetListAttributeValuesRequest {
        public List<ListItemInput> items;

        static class ListItemInput {
            public String enumValue;
            public String title;
            public String description;
        }
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