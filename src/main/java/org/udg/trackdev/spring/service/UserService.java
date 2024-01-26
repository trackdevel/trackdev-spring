package org.udg.trackdev.spring.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.GithubInfo;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.UserRepository;
import org.udg.trackdev.spring.utils.ErrorConstants;

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
    
    public User matchPassword(String email, String password) {
        User user = this.getByEmail(email);

        if (user == null) throw new ServiceException(ErrorConstants.LOGIN_KO);
        if (!user.getEnabled()) throw new ServiceException(ErrorConstants.USER_DISABLED);

        if (global.getPasswordEncoder().matches(password, user.getPassword()))
            return user;
        else
            throw new ServiceException(ErrorConstants.LOGIN_KO);
    }

    public boolean matchRecoveryCode(User user, String code) {
        return global.getPasswordEncoder().matches(code, user.getRecoveryCode());
    }

    @Transactional
    public User register(String username, String email) {
        try{
            checkIfExists(email);

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
            throw new ServiceException(ErrorConstants.REGISTER_KO + email);
        }

    }

    public User get(String id) {
        Optional<User> uo = repo().findById(id);
        if (uo.isPresent())
            return uo.get();
        else
            throw new EntityNotFound(String.format(ErrorConstants.USER_MAIL_NOT_FOUND, id));
    }

    public User getByUsername(String username) {
        Optional<User> user = repo().findByUsername(username);
        if(user.isEmpty()) {
            throw new EntityNotFound(String.format(ErrorConstants.USER_NOT_FOUND, username));
        }
        return user.get();
    }

    public User getByEmail(String email) {
        User user = repo().findByEmail(email);
        if(user == null) {
            throw new EntityNotFound(String.format(ErrorConstants.USER_NOT_FOUND, email));
        }
        return user;
    }

    public Boolean existsEmail(String email) {
        return repo().existsByEmail(email);
    }

    @Transactional
    public User addUserInternal(String username, String email, String password, List<UserType> roles) {
        checkIfExists(email);

        User user = new User(username, email, password);
        user.setChangePassword(false);
        user.setEnabled(true);

        for (UserType ut: roles) {
            Role role = roleService.get(ut);
            user.addRole(role);
        }
        repo().save(user);

        return user;
    }

    private void checkIfExists(String email) {
        if (repo().existsByEmail(email))
            throw new ServiceException(ErrorConstants.USER_ALREADY_EXIST + email);
    }

    @Transactional
    public void setLastLogin(User user) {
        user.setLastLogin(new Date());
        repo.save(user);
    }

    @Transactional
    public void setCurrentProject(User user, Project project) {
        user.setCurrentProject(project.getId());
        repo.save(user);
    }

    @Transactional
    public void changePassword(User user, String newpassword) {
        user.setPassword(global.getPasswordEncoder().encode(newpassword));
        user.setChangePassword(false);
        repo().save(user);
    }

    @Transactional
    public User editMyUser(User modifier, User user, Optional<String> username, Optional<String> color,
                         Optional<String> capitalLetters, Optional<Boolean> changePassword,
                         Optional<String> githubToken) {
        if(username != null && modifier.isUserType(UserType.ADMIN)) username.ifPresent(user::setUsername);
        if(color != null) color.ifPresent(user::setColor);
        if(capitalLetters != null) capitalLetters.ifPresent(user::setCapitalLetters);
        if(changePassword != null) changePassword.ifPresent(user::setChangePassword);
        if(githubToken != null) {
            githubToken.ifPresent(user::setGithubToken);
            ResponseEntity<GithubInfo> githubInfo = githubService.getGithubInformation(user.getGithubInfo().getGithub_token());
            if(githubInfo.getStatusCode().is2xxSuccessful()) {
                GithubInfo responseBody = githubInfo.getBody();
                if(responseBody != null) {
                    user.setGithubName(responseBody.getLogin());
                    user.setGithubAvatar(responseBody.getAvatar_url());
                    user.setGithubHtmlUrl(responseBody.getHtml_url());
                }
            }
            else if(githubInfo.getStatusCode().is4xxClientError()) {
                user.setGithubToken(ErrorConstants.GITHUB_TOKEN_INVALID);
                user.setGithubName(null);
                user.setGithubAvatar(null);
                user.setGithubHtmlUrl(null);
            }
            else {
                user.setGithubToken(ErrorConstants.API_GITHUB_KO);
                user.setGithubName(null);
                user.setGithubAvatar(null);
                user.setGithubHtmlUrl(null);
            }
        } else {
            user.setGithubToken(null);
            user.setGithubName(null);
            user.setGithubAvatar(null);
            user.setGithubHtmlUrl(null);
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
