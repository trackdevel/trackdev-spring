package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.InviteRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class InviteService extends BaseService<Invite, InviteRepository> {
    @Autowired
    RoleService roleService;

    @Autowired
    UserService userService;

    public List<Invite> searchCreated(String userId) {
        return super.search(InviteSpecs.isOwnedBy(userId));
    }

    public List<Invite> searchInvited(String userId) {
        User user = userService.get(userId);
        String email = user.getEmail();
        return super.search(InviteSpecs.isInvited(email));
    }

    @Transactional
    public Invite createInvite(String email, Collection<UserType> userTypes, String ownerId) {
        User owner = userService.get(ownerId);
        checkIfCanInviteUserTypes(owner.getRoles(), userTypes);
        userService.checkIfEmailExists(email);
        checkIfExists(email, ownerId);

        Invite invite = new Invite(email);
        for (UserType type : userTypes) {
            Role role = roleService.get(type);
            invite.addRole(role);
        }
        owner.addInvite(invite);
        invite.setOwner(owner);
        return invite;
    }

    private void checkIfExists(String email, String ownerId) {
        List<Invite> invites = inviteRepository.findByEmail(email);
        boolean alreadyInvited = invites.stream()
                .anyMatch(i -> i.getOwnerId().equals(ownerId));
        if(alreadyInvited) {
            throw new ServiceException("Invitation for this email already exists");
        }
    }

    private void checkIfCanInviteUserTypes(Set<Role> ownerRoles, Collection<UserType> invitationUserTypes) {
        boolean canInvite = false;
        for (Role ownerRole : ownerRoles) {
            canInvite = invitationUserTypes.stream()
                    .allMatch(iUT -> canInviteRole(ownerRole.getUserType(), iUT));
            if(canInvite) {
                break;
            }
        }
        if(!canInvite) {
            throw new ServiceException("User is not allowed to create an invitation for this role");
        }
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

    @Transactional
    public void deleteInvite(Long inviteId, String userId) {
        Invite invite = super.get(inviteId);
        if(!invite.getOwnerId().equals(userId)) {
            throw new ServiceException("User cannot manage invite");
        }
        repo.delete(invite);
    }

}
