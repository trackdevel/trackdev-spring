package org.udg.trackdev.spring.service;
import org.springframework.data.jpa.domain.Specification;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.User;

import java.util.List;

public abstract class InviteBuilder<R>
{
    UserService userService;
    InviteService inviteService;

    public InviteBuilder(UserService userService, InviteService inviteService) {
        this.userService = userService;
        this.inviteService = inviteService;
    }

    public Invite Build(String email, String ownerId, R resource) {
        User owner = userService.get(ownerId);
        checkIfCanInviteToResource(owner, resource);
        User invitee = userService.getByEmail(email);
        if(invitee != null) {
            checkInviteeAlreadyHasAccess(invitee, resource);
        }
        checkIfExistsSameOpenInvite(email, ownerId, resource);
        return BuildInvite(email, owner, resource);
    }

    private void checkIfExistsSameOpenInvite(String email, String ownerId, R resource) {
        Specification<Invite> spec = InviteSpecs.isInvited(email)
                .and(InviteSpecs.isPending())
                .and(isSameInvite(resource));
        List<Invite> invites = inviteService.searchCreated(ownerId, spec);
        if(invites.size() > 0) {
            throw new ServiceException("Same invitation for this email already exists");
        }
    }

    protected abstract void checkIfCanInviteToResource(User owner, R resource);

    protected abstract void checkInviteeAlreadyHasAccess(User invitee, R resource);
    
    protected abstract Specification<Invite> isSameInvite(R resource);

    protected abstract Invite BuildInvite(String email, User owner, R resource);
}
