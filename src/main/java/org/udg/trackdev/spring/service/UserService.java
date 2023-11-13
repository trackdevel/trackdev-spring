package org.udg.trackdev.spring.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.GithubInfo;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseServiceUUID<User, UserRepository> {

    @Autowired
    private RoleService roleService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private GithubService githubService;

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

    public boolean matchRecoveryCode(User user, String code) {
        return global.getPasswordEncoder().matches(code, user.getRecoveryCode());
    }

    /** COSA NOVA **/
    @Transactional
    public User register(String username, String email) {
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

    @Transactional
    public void setLastLogin(User user) {
        user.setLastLogin(new Date());
        repo.save(user);
    }

    @Transactional
    public void setCurrentProject(User user, Project project) {
        user.setCurrentProject(project);
        repo.save(user);
    }

    @Transactional
    public void changePassword(User user, String newpassword) {
        user.setPassword(global.getPasswordEncoder().encode(newpassword));
        user.setChangePassword(false);
        repo().save(user);
    }

    @Transactional
    public User editMyUser(User user, Optional<String> email, Optional<String> color,
                         Optional<String> capitalLetters, Optional<String> nicename, Optional<Boolean> changePassword,
                         Optional<String> githubToken) {
        if(email != null) email.ifPresent(user::setEmail);
        if(color != null) color.ifPresent(user::setColor);
        if(capitalLetters != null) capitalLetters.ifPresent(user::setCapitalLetters);
        if(nicename != null) nicename.ifPresent(user::setNicename);
        if(changePassword != null) changePassword.ifPresent(user::setChangePassword);
        if(githubToken != null) {
            githubToken.ifPresent(user::setGithubToken);
            ResponseEntity<GithubInfo> githubInfo = githubService.getGithubInformation(user.getGithubInfo().getGithub_token());
            if(githubInfo.getStatusCode().is2xxSuccessful()) {
                user.setGithubName(githubInfo.getBody().getLogin());
                user.setGithubAvatar(githubInfo.getBody().getAvatar_url());
                user.setGithubHtmlUrl(githubInfo.getBody().getHtml_url());
            }
            else if(githubInfo.getStatusCode().is4xxClientError()) {
                user.setGithubToken("ERROR: NOT VALID TOKEN");
                user.setGithubName(null);
                user.setGithubAvatar(null);
            }
            else {
                user.setGithubToken("ERROR: GITHUB API ERROR");
                user.setGithubName(null);
                user.setGithubAvatar(null);
            }
        }
        repo().save(user);
        return user;
    }

    @Transactional
    public String generateRecoveryCode(User user) {
        String code = RandomStringUtils.randomAlphanumeric(8);
        user.setRecoveryCode(global.getPasswordEncoder().encode(code));
        repo().save(user);
        return code;
    }

    public void cleanRecoveryCode(User user) {
        user.setRecoveryCode(null);
        repo().save(user);
    }

}
