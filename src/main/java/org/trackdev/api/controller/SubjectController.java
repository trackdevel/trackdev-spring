package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.IdResponseDTO;
import org.trackdev.api.dto.SubjectCompleteDTO;
import org.trackdev.api.dto.SubjectsResponseDTO;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.mapper.SubjectMapper;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.CourseService;
import org.trackdev.api.service.SubjectService;
import org.trackdev.api.service.UserService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for subject management.
 * Subjects can only be created, edited and deleted by admins.
 * Search is available for admins and professors only (not students).
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "3. Subjects")
@RestController
@RequestMapping(path = "/subjects")
public class SubjectController extends CrudController<Subject, SubjectService> {

    @Autowired
    CourseService courseService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    UserService userService;

    @Autowired
    SubjectMapper subjectMapper;

    @Operation(summary = "Search subjects", description = "Search subjects - admins, workspace admins, and professors only")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public SubjectsResponseDTO search(Principal principal, @RequestParam(name = "search", required = false) String search) {
        // Admins, workspace admins, and professors can see all subjects
        return new SubjectsResponseDTO(subjectMapper.toCompleteDTOList(super.search(search)));
    }

    @Operation(summary = "Get specific subject", description = "Get specific subject - admins, workspace admins, and professors only")
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public SubjectCompleteDTO getSubject(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        return subjectMapper.toCompleteDTO(service.getSubject(id, userId));
    }

    @Operation(summary = "Create subject", description = "Create subject - admins and workspace admins only")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN')")
    public IdResponseDTO createSubject(Principal principal, @Valid @RequestBody NewSubject subjectRequest) {
        String userId = super.getUserId(principal);
        Subject createdSubject = service.createSubject(subjectRequest.name, subjectRequest.acronym, userId);

        return new IdResponseDTO(createdSubject.getId());
    }

    @Operation(summary = "Edit specific subject", description = "Edit specific subject - admins and workspace admins only")
    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN')")
    public SubjectCompleteDTO editSubject(Principal principal,
                               @PathVariable(name = "id") Long id,
                               @Valid @RequestBody EditSubject subjectRequest,
                               BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        String userId = super.getUserId(principal);
        return subjectMapper.toCompleteDTO(service.editSubjectDetails(id, subjectRequest.name, subjectRequest.acronym, userId));
    }

    @Operation(summary = "Delete specific subject", description = "Delete specific subject - admins and workspace admins only")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN')")
    public ResponseEntity<Void> deleteSubject(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteSubject(id, userId);

        return okNoContent();
    }

    @Operation(summary = "Create course enrolled to specific subject", description = "Create course enrolled to specific subject")
    @PostMapping(path = "/{subjectId}/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public IdResponseDTO createCourse(Principal principal,
                                     @PathVariable(name = "subjectId") Long subjectId,
                                     @Valid @RequestBody NewCourse courseRequest,
                                     BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_COURSE_START_YEAR);
        }
        String userId = super.getUserId(principal);
        Course createdCourse = courseService.createCourse(subjectId, courseRequest.startYear, courseRequest.githubOrganization, userId);
        return new IdResponseDTO(createdCourse.getId());
    }

    static class NewSubject {
        @NotBlank
        @Size(
                min = Subject.MIN_NAME_LENGTH,
                max = Subject.NAME_LENGTH,
                message = ErrorConstants.INVALID_SUBJECT_NAME_LENGTH
        )
        public String name;
        @NotBlank
        @Size(
                min = Subject.MIN_ACRONYM_LENGTH,
                max = Subject.MAX_ACRONYM_LENGTH,
                message = ErrorConstants.INVALID_SUBJECT_ACRONYM_LENGTH
        )
        public String acronym;
    }

    static class EditSubject {
        @Size(
                min = Subject.MIN_NAME_LENGTH,
                max = Subject.NAME_LENGTH,
                message = ErrorConstants.INVALID_SUBJECT_NAME_LENGTH
        )
        public String name;
        @Size(
                min = Subject.MIN_ACRONYM_LENGTH,
                max = Subject.MAX_ACRONYM_LENGTH,
                message = ErrorConstants.INVALID_SUBJECT_ACRONYM_LENGTH
        )
        public String acronym;
    }

    static class NewCourse {
        @Min(value = Course.MIN_START_YEAR, message = ErrorConstants.INVALID_COURSE_START_YEAR)
        @Max(value = Course.MAX_START_YEAR, message = ErrorConstants.INVALID_COURSE_START_YEAR)
        public Integer startYear;
        public String githubOrganization;
    }
}