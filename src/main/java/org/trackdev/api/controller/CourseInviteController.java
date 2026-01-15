package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.CourseInviteDTO;
import org.trackdev.api.dto.CourseInvitesResponseDTO;
import org.trackdev.api.dto.InviteAcceptedResponseDTO;
import org.trackdev.api.entity.CourseInvite;
import org.trackdev.api.mapper.CourseInviteMapper;
import org.trackdev.api.service.CourseInviteService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

/**
 * Controller for course invitation management.
 * Handles sending and accepting course invitations.
 */
@Tag(name = "4.1 Course Invitations")
@RestController
@RequestMapping(path = "/courses")
public class CourseInviteController extends BaseController {

    @Autowired
    CourseInviteService courseInviteService;

    @Autowired
    CourseInviteMapper courseInviteMapper;

    // ==================== Course-specific invite endpoints ====================

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send invitations to students", description = "Send course invitations to a list of email addresses")
    @PostMapping(path = "/{courseId}/invites")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public CourseInvitesResponseDTO sendInvitations(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @Valid @RequestBody InviteStudentsRequest request,
            BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_MAIL_FORMAT);
        }
        String userId = super.getUserId(principal);
        List<CourseInvite> invites = courseInviteService.createInvitations(courseId, request.emails, userId);
        return new CourseInvitesResponseDTO(courseInviteMapper.toDTOList(invites));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get pending invites for a course", description = "Get all pending invitations for a specific course")
    @GetMapping(path = "/{courseId}/invites")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public CourseInvitesResponseDTO getPendingInvites(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        Collection<CourseInvite> invites = courseInviteService.getPendingInvites(courseId, userId);
        return new CourseInvitesResponseDTO(courseInviteMapper.toDTOList(invites));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all invites for a course", description = "Get all invitations (including accepted/expired) for a course")
    @GetMapping(path = "/{courseId}/invites/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public CourseInvitesResponseDTO getAllInvites(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId) {
        String userId = super.getUserId(principal);
        Collection<CourseInvite> invites = courseInviteService.getAllInvites(courseId, userId);
        return new CourseInvitesResponseDTO(courseInviteMapper.toDTOList(invites));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel an invitation", description = "Cancel a pending invitation")
    @DeleteMapping(path = "/{courseId}/invites/{inviteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> cancelInvitation(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "inviteId") Long inviteId) {
        String userId = super.getUserId(principal);
        courseInviteService.cancelInvitation(inviteId, userId);
        return okNoContent();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove student from course", description = "Remove a student from a course")
    @DeleteMapping(path = "/{courseId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> removeStudent(
            Principal principal,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "studentId") String studentId) {
        String userId = super.getUserId(principal);
        courseInviteService.removeStudent(courseId, studentId, userId);
        return okNoContent();
    }

    // ==================== Request/Response DTOs ====================

    static class InviteStudentsRequest {
        @NotEmpty
        public Collection<@Email String> emails;
    }

    static class AcceptInviteRequest {
        @Size(min = 8, message = ErrorConstants.PASSWORD_MINIUM_LENGTH)
        public String password;
    }
}
