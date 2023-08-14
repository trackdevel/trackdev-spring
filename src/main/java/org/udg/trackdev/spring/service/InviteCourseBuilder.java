package org.udg.trackdev.spring.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Courses;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.User;

@Component
public class InviteCourseBuilder extends InviteBuilder<Courses> {

    private AccessChecker accessChecker;

    public InviteCourseBuilder(UserService userService, InviteService inviteService, AccessChecker accessChecker) {
        super(userService, inviteService);
        this.accessChecker = accessChecker;
    }

    @Override
    protected void checkIfCanInviteToResource(User owner, Courses resource) {
        accessChecker.checkCanManageCourseYear(resource, owner.getId());
    }

    @Override
    protected void checkInviteeAlreadyHasAccess(User invitee, Courses courses) {
        for(Courses cy: invitee.getEnrolledCourseYears()) {
            if(cy.getId().equals(courses.getId())) {
                throw new ServiceException("User is already enrolled in course");
            }
        }
    }

    @Override
    protected Specification<Invite> isSameInvite(Courses courses) {
        return InviteSpecs.forCourseYear(courses.getId());
    }

    @Override
    protected Invite BuildInvite(String email, User owner, Courses courses) {
        Invite invite = new Invite(email);
        invite.setCourseYear(courses);
        owner.addInvite(invite);
        invite.setOwner(owner);
        return invite;
    }
}
