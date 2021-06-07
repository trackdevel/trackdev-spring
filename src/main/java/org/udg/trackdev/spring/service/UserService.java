package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.repository.InviteRepository;
import org.udg.trackdev.spring.repository.RoleRepository;
import org.udg.trackdev.spring.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    Global global;

    public UserRepository crud() {
        return userRepository;
    }

    public void checkIfEmailExists(String email) {
        if (userRepository.existsByEmail(email))
            throw new ServiceException("User with this email already exist");
    }

    public User matchPassword(String username, String password) {

        User user = userRepository.findByUsername(username);

        if (user == null) throw new ServiceException("User does not exists");

        if (global.getPasswordEncoder().matches(password, user.getPassword()))
            return user;
        else
            throw new ServiceException("Password does not match");
    }

    @Transactional
    public User register(String username, String email, String password) {
        checkIfExists(username, email);

        List<Invite> invites = inviteRepository.findByEmail(email);

        if (invites.size() == 0) throw new ServiceException("This email does not have any invite");

        User User = new User(username, email, global.getPasswordEncoder().encode(password));
        for (Invite invite: invites) {
            Role role = new Role(invite.getUserType());
            User.addRole(role);
        }

        userRepository.save(User);
        return User;
    }

    public User get(String id) {
        Optional<User> uo = userRepository.findById(id);
        if (uo.isPresent())
            return uo.get();
        else
            throw new ServiceException(String.format("User with id = %s dos not exists", id));
    }

    @Transactional
    public User addUserInternal(String username, String email, String password, List<UserType> roles) {
        checkIfExists(username, email);

        User user = new User(username, email, password);
        for (UserType ut: roles) {
            Role role = new Role(ut);
            user.addRole(role);
        }
        userRepository.save(user);

        return user;
    }

    private void checkIfExists(String username, String email) {
        checkIfEmailExists(email);

        if (userRepository.existsByUsername(username))
            throw new ServiceException("Tname already exists");
    }

}
