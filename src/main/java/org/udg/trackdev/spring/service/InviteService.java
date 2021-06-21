package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.InviteRepository;

import java.util.Collection;
import java.util.List;

@Service
public class InviteService extends BaseService<Invite, InviteRepository> {
    @Autowired
    RoleService roleService;

    @Autowired
    UserService userService;

    @Autowired
    InviteRoleBuilder roleInviteBuilder;

    public List<Invite> searchCreated(String userId, Specification<Invite> specification) {
        return super.search(InviteSpecs.isOwnedBy(userId).and(specification));
    }

    public List<Invite> searchInvited(String userId, Specification<Invite> specification) {
        User user = userService.get(userId);
        String email = user.getEmail();
        return super.search(InviteSpecs.isInvited(email).and(specification));
    }

    public List<Invite> searchByEmail(String email) {
        return super.search(InviteSpecs.isInvited(email));
    }

    @Transactional
    public Invite createInvite(String email, Collection<UserType> userTypes, String ownerId) {
        Invite invite = roleInviteBuilder.Build(email, ownerId, userTypes);
        return invite;
    }

    @Transactional
    public void deleteInvite(Long inviteId, String userId) {
        Invite invite = super.get(inviteId);
        if(!invite.getOwnerId().equals(userId)) {
            throw new ServiceException("User cannot manage invite");
        }
        if(invite.getState() != InviteState.PENDING) {
            throw new ServiceException("Only pending invites can be deleted");
        }
        repo.delete(invite);
    }

    @Transactional
    public void acceptInvite(Long inviteId, String userId) {
        Invite invite = get(inviteId);
        User user = userService.get(userId);
        useInvite(invite, user);
    }

    @Transactional
    public void useInvite(Invite invite, User user) {
        if(!user.getEmail().equals(invite.getEmail())) {
            throw new ServiceException("User cannot accept an invite that is not for them");
        }
        if(invite.getState() != InviteState.PENDING) {
            throw new ServiceException("Invite cannot be used");
        }
        for(Role inviteRole : invite.getRoles()) {
            user.addRole(inviteRole);
        }
        if(invite.getCourseYear() != null) {
            if(!user.isUserType(UserType.STUDENT)) {
                Role role = roleService.get(UserType.STUDENT);
                user.addRole(role);
            }
            user.enrollToCourseYear(invite.getCourseYear());
        }
        invite.use();
    }
}
