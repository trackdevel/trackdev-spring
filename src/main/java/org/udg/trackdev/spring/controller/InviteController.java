package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.IdObjectLong;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.service.InviteService;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/invites")
public class InviteController {

    @Autowired
    InviteService service;

    @PostMapping()
    public IdObjectLong createInvite(@Valid @RequestBody NewInvite inviteRequest) {
        Invite createdInvite = service.createInvite(inviteRequest.email, inviteRequest.role);
        return new IdObjectLong(createdInvite.getId());
    }

    static class NewInvite {
        @NotNull
        @Email
        public String email;

        @NotNull
        public UserType role;
    }
}