package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.IdObjectLong;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.service.InviteService;
import org.udg.trackdev.spring.service.InviteSpecs;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping()
public class InviteController extends BaseController {

    @Autowired
    InviteService service;

    @GetMapping(path = "/invites")
    public List<Invite> getInvites(Principal principal,
                               @RequestParam(name = "type", required = false) String type,
                               @RequestParam(name = "courseYearId", required = false) Long yearId) {
        String userId = super.getUserId(principal);
        Specification<Invite> specification = buildSpecification(type, yearId);
        List<Invite> invites = service.searchCreated(userId, specification);
        return invites;
    }

    @PostMapping(path = "/invites")
    public IdObjectLong createInvite(Principal principal, @Valid @RequestBody NewInvite inviteRequest) {
        String userId = super.getUserId(principal);
        Invite createdInvite = service.createInvite(inviteRequest.email, inviteRequest.roles, userId);
        return new IdObjectLong(createdInvite.getId());
    }

    @DeleteMapping(path = "/invites/{inviteId}")
    public ResponseEntity getInvites(Principal principal, @PathVariable("inviteId") Long inviteId) {
        String userId = super.getUserId(principal);
        service.deleteInvite(inviteId, userId);
        return okNoContent();
    }

    @GetMapping(path = "/users/self/invites")
    public List<Invite> getSelfInvites(Principal principal,
                               @RequestParam(name = "type", required = false) String type,
                               @RequestParam(name = "courseYearId", required = false) Long yearId) {
        String userId = super.getUserId(principal);
        Specification<Invite> specification = buildSpecification(type, yearId);
        List<Invite> invites = service.searchInvited(userId, specification);
        return invites;
    }

    @PatchMapping(path = "/users/self/invites/{inviteId}")
    public ResponseEntity acceptInvite(Principal principal, @PathVariable("inviteId") Long inviteId) {
        String userId = super.getUserId(principal);
        service.acceptInvite(inviteId, userId);
        return okNoContent();
    }

    private Specification<Invite> buildSpecification(String type, Long yearId) {
        Specification<Invite> spec = InviteSpecs.isPending(); // Only show open invites
        if(type != null) {
            switch (type) {
                case "role":
                    spec = spec.and(InviteSpecs.notForCourseYear());
                    break;
                case "courseYear":
                    spec = spec.and(InviteSpecs.forCourseYear());
            }
        }
        if(yearId != null) {
            spec = spec.and(InviteSpecs.forCourseYear(yearId));
        }
        return spec;
    }

    static class NewInvite {
        @NotNull
        @Email
        @Size(max = User.EMAIL_LENGTH)
        public String email;

        @NotEmpty
        public Collection<UserType> roles;
    }
}