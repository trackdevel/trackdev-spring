package org.udg.trackdev.spring.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Invite;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.repository.UserRepository;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseServiceUUID<User, UserRepository> {

    @Autowired
    private EntityManager em;

    @Autowired
    private RoleService roleService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private AccessChecker accessChecker;

    @Autowired
    Global global;
    
    public User matchPassword(String username, String password) {
        User user = this.getByUsername(username);

        if (user == null) throw new ServiceException("User does not exists");

        if (global.getPasswordEncoder().matches(password, user.getPassword()))
            return user;
        else
            throw new ServiceException("Password does not match");
    }

    @Transactional
    public User register(String username, String email, String password) {
        checkIfExists(username, email);

        List<Invite> invites = inviteService.searchByEmail(email);
        if (invites.size() == 0) throw new ServiceException("This email does not have any invite");

        User user = new User(username, email, global.getPasswordEncoder().encode(password));
        for (Invite invite: invites) {
            inviteService.useInvite(invite, user);
        }
        repo().save(user);
        return user;
    }

    /** COSA NOVA **/
    @Transactional
    public User registerv2(String username, String email) {
        try{
            checkIfExists(username, email);

            String tempPassword = RandomStringUtils.randomAlphanumeric(8);

            User user = new User(username, email, global.getPasswordEncoder().encode(tempPassword));
            user.setChangePassword(true);
            user.setEnabled(true);
            user.addRole(roleService.get(UserType.STUDENT));
            repo().save(user);

            emailSenderService.sendRegisterEmail(username,email,tempPassword);

            return user;
        }
        catch (Exception e) {
            throw new ServiceException("Error when registering user" + e.getMessage());
        }

    }

    public User get(String id) {
        Optional<User> uo = repo().findById(id);
        if (uo.isPresent())
            return uo.get();
        else
            throw new EntityNotFound(String.format("User with id = %s does not exist", id));
    }

    public User getByUsername(String username) {
        Optional<User> user = repo().findByUsername(username);
        if(user.isEmpty()) {
            throw new EntityNotFound(String.format("User with username <%s> does not exist", username));
        }
        return user.get();
    }

    public User getByEmail(String email) {
        User user = repo().findByEmail(email);
        if(user == null) {
            throw new EntityNotFound(String.format("User with name = %s does not exist", email));
        }
        return user;
    }

    public Boolean existsEmail(String email) {
        return repo().existsByEmail(email);
    }

    @Transactional
    public User addUserInternal(String username, String email, String password, List<UserType> roles) {
        checkIfExists(username, email);

        /** COSES NOVES **/
        User user = new User(username, email, password);
        user.setChangePassword(true);
        user.setEnabled(true);
        user.setNicename("Test User Dos");
        /**************/

        for (UserType ut: roles) {
            Role role = roleService.get(ut);
            user.addRole(role);
        }
        repo().save(user);

        return user;
    }

    private void checkIfExists(String username, String email) {
        if (repo().existsByEmail(email))
            throw new ServiceException("User with this email already exist");

        if (repo().existsByUsername(username))
            throw new ServiceException("Username already exists");
    }

    public User getByGithubName(String githubName) {
        return this.repo().findByGithubName(githubName).orElseThrow(
                () -> new ServiceException(String.format("User with githb name = %s does not exists", githubName)));
    }

    @Transactional
    public void setLastLogin(User user) {
        ZonedDateTime currentDateTimeSpain = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
        LocalDateTime localDateTimeSpain = currentDateTimeSpain.toLocalDateTime();
        user.setLastLogin(localDateTimeSpain);
        em.persist(user);
    }

}
