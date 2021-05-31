package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.repository.InviteRepository;

@Service
public class InviteService {

    @Autowired
    InviteRepository inviteRepository;

    public Invite createInvite(String email, UserType role) {
        Invite invite = new Invite(role);
        invite.setEmail(email);
        inviteRepository.save(invite);
        return invite;
    }
}
