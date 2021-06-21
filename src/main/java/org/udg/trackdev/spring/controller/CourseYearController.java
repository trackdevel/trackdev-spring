package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.service.CourseYearService;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.security.Principal;

@RestController
@RequestMapping(path = "/courses/years")
public class CourseYearController extends BaseController {

    @Autowired
    CourseYearService courseYearService;

    @PostMapping(path = "/{yearId}/invites")
    public IdObjectLong createInvite(Principal principal,
                                    @PathVariable("yearId") Long yearId,
                                    @Valid @RequestBody NewCourseInvite inviteRequest) {
        String userId = super.getUserId(principal);
        Invite createdInvite = courseYearService.createInvite(inviteRequest.email, yearId, userId);
        return new IdObjectLong(createdInvite.getId());
    }

    static class NewCourseInvite {
        @NotNull
        @Email
        @Size(max = User.EMAIL_LENGTH)
        public String email;

        public Long courseYearId;
    }
}