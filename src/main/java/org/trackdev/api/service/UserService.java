package org.trackdev.api.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.SecurityException;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.GithubInfo;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Role;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.UserRepository;
import org.trackdev.api.utils.ErrorConstants;

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
    private PasswordEncoder passwordEncoder;
    
    public User matchPassword(String email, String password) {
        User user = this.getByEmail(email);

        if (user == null) throw new ServiceException(ErrorConstants.LOGIN_KO);
        if (!user.getEnabled()) throw new SecurityException(ErrorConstants.USER_DISABLED);

        if (passwordEncoder.matches(password, user.getPassword()))
            return user;
        else
            throw new ServiceException(ErrorConstants.LOGIN_KO);
    }

    public boolean matchRecoveryCode(User user, String code) {
        return passwordEncoder.matches(code, user.getRecoveryCode());
    }

    @Transactional
    public User register(String username, String email, String password, UserType userType) {
        try{
            checkIfExists(email);

            User user = new User(username, email, passwordEncoder.encode(password));
            user.setChangePassword(false);
            user.setEnabled(true);
            user.addRole(roleService.get(userType));
            repo().save(user);

            emailSenderService.sendRegisterEmail(username, email, password, "en");

            return user;
        }
        catch (Exception e) {
            throw new ServiceException(ErrorConstants.REGISTER_KO + ": " + email, e);
        }

    }

    public User get(String id) {
        Optional<User> uo = repo().findById(id);
        if (uo.isPresent())
            return uo.get();
        else
            throw new EntityNotFound(ErrorConstants.USER_MAIL_NOT_FOUND.formatted(id));
    }

    public User getByUsername(String username) {
        Optional<User> user = repo().findByUsername(username);
        if(user.isEmpty()) {
            throw new EntityNotFound(ErrorConstants.USER_NOT_FOUND.formatted(username));
        }
        return user.get();
    }

    public User getByEmail(String email) {
        User user = repo().findByEmail(email);
        if(user == null) {
            throw new EntityNotFound(ErrorConstants.USER_NOT_FOUND.formatted(email));
        }
        return user;
    }

    public Boolean existsEmail(String email) {
        return repo().existsByEmail(email);
    }

    @Transactional
    public User save(User user) {
        return repo().save(user);
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
        user.setPassword(passwordEncoder.encode(newpassword));
        user.setChangePassword(false);
        repo().save(user);
    }

    /**
     * Change password with old password verification.
     * All operations in a single transaction.
     */
    @Transactional
    public void changePasswordWithVerification(String userId, String oldPassword, String newPassword) {
        User user = get(userId);
        matchPassword(user.getEmail(), oldPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setChangePassword(false);
        repo().save(user);
    }

    /**
     * Recover password with recovery code verification.
     * All operations in a single transaction.
     */
    @Transactional
    public void recoverPassword(String email, String code, String newPassword) {
        User user = getByEmail(email);
        if (!matchRecoveryCode(user, code)) {
            throw new ServiceException(ErrorConstants.RECOVERY_CODE_NOT_MATCH);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setChangePassword(false);
        user.setRecoveryCode(null);
        repo().save(user);
    }

    @Transactional
    public User editMyUser(User modifier, User user, Optional<String> username, Optional<String> color,
                         Optional<String> capitalLetters, Optional<Boolean> changePassword,
                         Optional<String> githubToken, Optional<Boolean> enabled) {
        if(username != null && modifier.isUserType(UserType.ADMIN)) username.ifPresent(user::setUsername);
        if(color != null) color.ifPresent(user::setColor);
        if(capitalLetters != null) capitalLetters.ifPresent(user::setCapitalLetters);
        if(changePassword != null) changePassword.ifPresent(user::setChangePassword);
        if(enabled != null && modifier.isUserType(UserType.ADMIN)) enabled.ifPresent(user::setEnabled);
        if(githubToken != null && githubToken.isPresent()) {
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
        String code = RandomStringUtils.secure().nextAlphanumeric(8);
        user.setRecoveryCode(passwordEncoder.encode(code));
        repo().save(user);
        return code;
    }

    public void cleanRecoveryCode(User user) {
        user.setRecoveryCode(null);
        repo().save(user);
    }

    /**
     * Get all users with a specific user type.
     */
    public List<User> getUsersByType(UserType userType) {
        return repo.findByRoles_UserType(userType);
    }

    /**
     * Edit own user profile.
     * All operations in a single transaction.
     */
    @Transactional
    public User editMyUserById(String userId, Optional<String> username, Optional<String> color,
                               Optional<String> capitalLetters, Optional<Boolean> changePassword,
                               Optional<String> githubToken, Optional<Boolean> enabled) {
        User user = get(userId);
        return editMyUser(user, user, username, color, capitalLetters, changePassword, githubToken, enabled);
    }

    /**
     * Edit another user's profile (admin only).
     * All operations in a single transaction.
     */
    @Transactional
    public User editUserByAdmin(String adminUserId, String targetUserId, Optional<String> username, 
                                Optional<String> color, Optional<String> capitalLetters, 
                                Optional<Boolean> changePassword, Optional<String> githubToken, 
                                Optional<Boolean> enabled) {
        User modifier = get(adminUserId);
        accessChecker.checkIsUserAdmin(modifier);
        User user = get(targetUserId);
        return editMyUser(modifier, user, username, color, capitalLetters, changePassword, githubToken, enabled);
    }

    /**
     * Check if user is admin. Returns the result.
     * All operations in a single transaction.
     */
    @Transactional(readOnly = true)
    public boolean isUserAdmin(String userId) {
        User user = get(userId);
        return accessChecker.isUserAdmin(user);
    }

    /**
     * Delete a user after checking for dependencies.
     * A user can only be deleted if they have no:
     * - Owned subjects
     * - Reported tasks
     * - Assigned tasks
     * - Authored comments
     * - Sent course invites
     * - Owned courses
     * 
     * Users are automatically removed from:
     * - Project memberships (ManyToMany)
     * - Course enrollments (ManyToMany)
     * 
     * @param userId The ID of the user to delete
     * @throws ServiceException if user has dependencies that prevent deletion
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = get(userId);
        
        // Check for owned subjects (field is subjectsOwns, not private getter)
        long ownedSubjectsCount = repo().countSubjectsOwnedByUser(userId);
        if (ownedSubjectsCount > 0) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_USER_HAS_SUBJECTS);
        }
        
        // Check for reported tasks
        long reportedTasksCount = repo().countTasksReportedByUser(userId);
        if (reportedTasksCount > 0) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_USER_HAS_REPORTED_TASKS);
        }
        
        // Check for assigned tasks
        long assignedTasksCount = repo().countTasksAssignedToUser(userId);
        if (assignedTasksCount > 0) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_USER_HAS_ASSIGNED_TASKS);
        }
        
        // Check for authored comments (field is comments, final field)
        long commentsCount = repo().countCommentsAuthoredByUser(userId);
        if (commentsCount > 0) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_USER_HAS_COMMENTS);
        }
        
        // Check for sent course invites
        long sentInvitesCount = repo().countInvitesSentByUser(userId);
        if (sentInvitesCount > 0) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_USER_HAS_SENT_INVITES);
        }
        
        // Check for owned courses
        long ownedCoursesCount = repo().countCoursesOwnedByUser(userId);
        if (ownedCoursesCount > 0) {
            throw new ServiceException(ErrorConstants.CANNOT_DELETE_USER_HAS_OWNED_COURSES);
        }
        
        // Remove from all project memberships (ManyToMany will handle)
        for (Project project : user.getProjects()) {
            project.getMembers().remove(user);
        }
        user.getProjects().clear();
        
        // Delete the user (cascade will handle GithubInfo, PointsReview)
        repo().delete(user);
    }

}
