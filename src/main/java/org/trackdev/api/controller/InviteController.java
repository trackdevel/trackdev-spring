package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.InviteAcceptedResponseDTO;
import org.trackdev.api.entity.CourseInvite;
import org.trackdev.api.service.CourseInviteService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * Public controller for accepting course invitations.
 * These endpoints don't require authentication.
 */
@Tag(name = "0. Public")
@RestController
@RequestMapping(path = "/invites")
public class InviteController extends BaseController {

    @Autowired
    CourseInviteService courseInviteService;

    @Operation(summary = "Get invite details", description = "Get details about an invitation by token (public endpoint)")
    @GetMapping(path = "/{token}")
    public InviteInfoResponse getInviteInfo(@PathVariable String token) {
        CourseInvite invite = courseInviteService.getInviteByToken(token);
        return new InviteInfoResponse(
            invite.getEmail(),
            invite.getCourse().getSubject() != null ? invite.getCourse().getSubject().getName() : "Course",
            invite.getCourse().getStartYear(),
            invite.getStatus().toString(),
            invite.isExpired(),
            invite.getInvitedBy().getUsername()
        );
    }

    @Operation(summary = "Accept invitation", description = "Accept a course invitation. If user doesn't exist, provide password to create account.")
    @PostMapping(path = "/{token}/accept")
    public InviteAcceptedResponseDTO acceptInvitation(
            @PathVariable String token,
            @Valid @RequestBody(required = false) AcceptInviteRequest request) {
        String password = request != null ? request.password : null;
        CourseInviteService.AcceptInviteResult result = courseInviteService.acceptInvitation(token, password);
        
        String message = result.isNewUserCreated() 
            ? "Account created and enrolled in course successfully"
            : "Successfully enrolled in course";
            
        return new InviteAcceptedResponseDTO(
            result.getCourseId(),
            result.getCourseName(),
            result.getStartYear(),
            result.isNewUserCreated(),
            result.isPasswordChangeRequired(),
            message
        );
    }

    // ==================== Request/Response DTOs ====================

    public static class InviteInfoResponse {
        public String email;
        public String courseName;
        public Integer startYear;
        public String status;
        public boolean expired;
        public String invitedBy;

        public InviteInfoResponse(String email, String courseName, Integer startYear, 
                                   String status, boolean expired, String invitedBy) {
            this.email = email;
            this.courseName = courseName;
            this.startYear = startYear;
            this.status = status;
            this.expired = expired;
            this.invitedBy = invitedBy;
        }
    }

    static class AcceptInviteRequest {
        @Size(min = 8, message = ErrorConstants.PASSWORD_MINIUM_LENGTH)
        public String password;
    }
}
