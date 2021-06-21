package org.udg.trackdev.spring.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.User;

@Component
public class InviteCourseBuilder extends InviteBuilder<CourseYear> {

    private CourseService courseService;

    public InviteCourseBuilder(UserService userService, InviteService inviteService, CourseService courseService) {
        super(userService, inviteService);
        this.courseService = courseService;
    }

    @Override
    protected void checkIfCanInviteToResource(User owner, CourseYear resource) {
        if(!courseService.canManageCourse(resource.getCourse(), owner.getId())) {
            throw new ServiceException("User cannot manage this course");
        }
    }

    @Override
    protected void checkInviteeAlreadyHasAccess(User invitee, CourseYear courseYear) {
        for(CourseYear cy: invitee.getCourseYears()) {
            if(cy.getId().equals(courseYear.getId())) {
                throw new ServiceException("User is already enrolled in course");
            }
        }
    }

    @Override
    protected Specification<Invite> isSameInvite(CourseYear courseYear) {
        return InviteSpecs.forCourseYear(courseYear.getId());
    }

    @Override
    protected Invite BuildInvite(String email, User owner, CourseYear courseYear) {
        Invite invite = new Invite(email);
        invite.setCourseYear(courseYear);
        owner.addInvite(invite);
        invite.setOwner(owner);
        return invite;
    }
}
