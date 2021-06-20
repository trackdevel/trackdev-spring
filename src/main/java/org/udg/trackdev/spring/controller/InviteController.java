package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.IdObjectLong;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.service.InviteService;

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
    public List<Invite> getInvites(Principal principal) {
        String userId = super.getUserId(principal);
        List<Invite> invites = service.searchCreated(userId);
        return invites;
    }

    @PostMapping(path = "/invites")
    public IdObjectLong createInvite(Principal principal, @Valid @RequestBody NewInvite inviteRequest) {
        String userId = super.getUserId(principal);
        Invite createdInvite = service.createInvite(inviteRequest.email, inviteRequest.roles, userId);
        return new IdObjectLong(createdInvite.getId());
    }

    @GetMapping(path = "/users/self/invites")
    public List<Invite> getSelfInvites(Principal principal) {
        String userId = super.getUserId(principal);
        List<Invite> invites = service.searchInvited(userId);
        return invites;
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