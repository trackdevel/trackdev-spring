package org.udg.trackdev.spring.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class InviteRoleBuilder extends InviteBuilder<Collection<UserType>> {

    private RoleService roleService;

    public InviteRoleBuilder(UserService userService, InviteService inviteService, RoleService roleService) {
        super(userService, inviteService);
        this.roleService = roleService;
    }

    @Override
    protected void checkIfCanInviteToResource(User owner, Collection<UserType> userTypes) {
        for (UserType inviteUserType : userTypes) {
            boolean canInvite = owner.getRoles().stream()
                    .anyMatch(r -> canInviteRole(r.getUserType(), inviteUserType));
            if(!canInvite)
                throw new ServiceException("User is not allowed to create an invitation for this role");
        }
    }

    @Override
    protected void checkInviteeAlreadyHasAccess(User invitee, Collection<UserType> userTypes) {
        Boolean containsAll = true;
        for(UserType ut: userTypes) {
            if(!invitee.isUserType(ut)) {
                containsAll = false;
                break;
            }
        }
        if(containsAll)
            throw new ServiceException("User already exists with these roles");
    }

    @Override
    protected Specification<Invite> isSameInvite(Collection<UserType> userTypes) {
        return InviteSpecs.notForCourseYear();
    }

    @Override
    protected Invite BuildInvite(String email, User owner, Collection<UserType> userTypes) {
        Invite invite = new Invite(email);
        for (UserType type : userTypes) {
            Role role = roleService.get(type);
            invite.addRole(role);
        }
        owner.addInvite(invite);
        invite.setOwner(owner);
        return invite;
    }

    private boolean canInviteRole(UserType ownerType, UserType invitationRole) {
        List<UserType> allowedRoles = new ArrayList<UserType>();
        switch (ownerType) {
            case ADMIN:
                allowedRoles.add(UserType.ADMIN);
                allowedRoles.add(UserType.PROFESSOR);
                break;
            case PROFESSOR:
                allowedRoles.add(UserType.PROFESSOR);
                allowedRoles.add(UserType.STUDENT);
                break;
            case STUDENT:
                break;
        }
        boolean canInvite = allowedRoles.stream().anyMatch(s -> s == invitationRole);
        return canInvite;
    }
}
