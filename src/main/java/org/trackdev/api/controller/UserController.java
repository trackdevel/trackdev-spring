package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.dto.UserWithGithubTokenDTO;
import org.trackdev.api.dto.UserWithProjectsDTO;
import org.trackdev.api.dto.UsersResponseDTO;
import org.trackdev.api.entity.User;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.mapper.UserMapper;
import org.trackdev.api.model.response.AdminCheckResponse;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.UserService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for user management.
 * Uses both Spring Security annotations and AccessChecker for defense in depth.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "2. Users")
@RequestMapping(path = "/users")
@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    UserMapper userMapper;

    /**
     * Returns the public profile of any user.
     * @param principal The current authenticated entity
     * @param id The email of the user to request.
     * @return The User identified by username
     */
    @Operation(summary = "Get user by id", description = "Get user by id")
    @GetMapping(path = "/uuid/{id}")
    public UserWithProjectsDTO getPublic(Principal principal, @PathVariable(name = "id") String id) {
        super.checkLoggedIn(principal);
        return userMapper.toWithProjectsDTO(userService.get(id));
    }

    /**
     * Returns the public profile of any user.
     * @param principal The current authenticated entity
     * @param email The email of the user to request.
     * @return The User identified by username
     */
    @Operation(summary = "Get user by email", description = "Get user by email")
    @GetMapping(path = "/{email}")
    public UserWithProjectsDTO getUserEmail(Principal principal, @PathVariable(name = "email") String email) {
        super.checkLoggedIn(principal);
        return userMapper.toWithProjectsDTO(userService.getByEmail(email));
    }

    @Operation(summary = "Get all users", description = "Get all users, only admins can do this")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UsersResponseDTO getAll(Principal principal) {
        // Double-check with AccessChecker for defense in depth
        if (!accessChecker.isUserAdmin(userService.get(principal.getName()))){
            throw new SecurityException(ErrorConstants.EMPTY);
        }
        return new UsersResponseDTO(userMapper.toWithProjectsDTOList(userService.findAll()));
    }

    @Operation(summary = "Get workspace users", description = "Get WORKSPACE_ADMIN and PROFESSOR users from the current user's workspace. Only workspace admins can do this.")
    @GetMapping(path = "/workspace")
    @PreAuthorize("hasRole('WORKSPACE_ADMIN')")
    public UsersResponseDTO getWorkspaceUsers(Principal principal) {
        String userId = super.getUserId(principal);
        User currentUser = userService.get(userId);
        
        // Check that user has a workspace
        if (currentUser.getWorkspace() == null) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        Long workspaceId = currentUser.getWorkspace().getId();
        return new UsersResponseDTO(userMapper.toWithProjectsDTOList(userService.getWorkspaceUsers(workspaceId)));
    }

    @Operation(summary = "Register user", description = "Register user. Admins can create any user type. Workspace admins can create professors and students in their workspace. Professors can create students for their courses.")
    @PostMapping(path = "/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> register(Principal principal, @Valid @RequestBody RegisterU ru,
                                         BindingResult result) {
        checkLoggedIn(principal);
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        
        // Use AccessChecker for authorization
        accessChecker.checkCanCreateUser(currentUser, ru.userType, ru.workspaceId, ru.courseId);
        
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        userService.register(ru.username, ru.fullName, ru.email, ru.password, ru.userType, ru.workspaceId, ru.courseId);
        return okNoContent();
    }

    @Operation(summary = "Edit your user", description = "Edit your user")
    @PatchMapping
    public UserWithGithubTokenDTO editMyUser(Principal principal,
                         @Valid @RequestBody EditU userRequest) {
        if (userRequest.username != null){
            if (userRequest.username.get().isEmpty() || userRequest.username.get().length() > User.USERNAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_SIZE);
            }
            if (!userRequest.username.get().matches(User.USERNAME_PATTERN)) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_FORMAT);
            }
        }
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        return userMapper.toWithGithubTokenDTO(userService.editMyUserById(userId, userRequest.username, userRequest.color,
                userRequest.capitalLetters, userRequest.changePassword, userRequest.githubToken, userRequest.enabled));
    }

    @Operation(summary = "Edit user", description = "Edit user, only admins can do this")
    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserWithProjectsDTO editUser(Principal principal,
                            @Valid @RequestBody EditU userRequest,
                            @PathVariable(name = "id") String id) {
        if (userRequest.username != null){
            if (userRequest.username.get().isEmpty() || userRequest.username.get().length() > User.USERNAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_SIZE);
            }
            if (!userRequest.username.get().matches(User.USERNAME_PATTERN)) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_FORMAT);
            }
        }
        String userId = super.getUserId(principal);
        // All operations in a single transaction with admin check inside service
        return userMapper.toWithProjectsDTO(userService.editUserByAdmin(userId, id, userRequest.username, userRequest.color,
                userRequest.capitalLetters, userRequest.changePassword, userRequest.githubToken, userRequest.enabled));
    }

    @Operation(summary = "Check if authenticated user is admin", description = "Check if authenticated user is admin")
    @GetMapping(path = "/checker/admin")
    public AdminCheckResponse imAdminUser(Principal principal) {
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        return new AdminCheckResponse(userService.isUserAdmin(userId));
    }

    static class RegisterU {
        @NotBlank
        @Size(
                min = User.MIN_USERNAME_LENGTH,
                max = User.USERNAME_LENGTH,
                message = ErrorConstants.INVALID_USERNAME_SIZE
        )
        @Pattern(
                regexp = User.USERNAME_PATTERN,
                message = ErrorConstants.INVALID_USERNAME_FORMAT
        )
        public String username;

        @NotBlank
        @Size(
                min = User.MIN_FULL_NAME_LENGTH,
                max = User.FULL_NAME_LENGTH,
                message = ErrorConstants.INVALID_FULL_NAME_SIZE
        )
        public String fullName;

        @NotBlank
        @Email(message = ErrorConstants.INVALID_MAIL_FORMAT)
        @Size(
                min = User.MIN_EMAIL_LENGHT,
                max = User.EMAIL_LENGTH,
                message = ErrorConstants.INVALID_MAIL_SIZE
        )
        public String email;

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        public String password;

        @NotNull
        public UserType userType;

        // Optional: workspace ID for WORKSPACE_ADMIN creating users in their workspace
        public Long workspaceId;

        // Optional: course ID for PROFESSOR creating students for their course
        public Long courseId;

    }

    static class EditU {

        public Optional<String> username;

        public Optional<String> color;

        public Optional<String> capitalLetters;

        public Optional<Boolean> changePassword;

        public Optional<String> githubToken;

        public Optional<Boolean> enabled;
    }

    /**
     * Delete a user. Admins can delete any user.
     * Workspace admins can delete PROFESSOR users from their workspace.
     * User can only be deleted if they have no dependencies.
     */
    @Operation(summary = "Delete user", description = "Delete a user if they have no dependencies")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(Principal principal, @PathVariable(name = "id") String id) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        User targetUser = userService.get(id);
        
        accessChecker.checkCanManageWorkspaceUser(currentUser, targetUser);
        
        userService.deleteUser(id);
        return okNoContent();
    }

    /**
     * Edit a user. Admins can edit any user.
     * Workspace admins can edit PROFESSOR users from their workspace.
     */
    @Operation(summary = "Edit user by workspace admin", description = "Edit a user from the workspace")
    @PatchMapping(path = "/workspace/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKSPACE_ADMIN')")
    public UserWithProjectsDTO editWorkspaceUser(Principal principal,
                            @Valid @RequestBody EditU userRequest,
                            @PathVariable String id) {
        if (userRequest.username != null){
            if (userRequest.username.get().isEmpty() || userRequest.username.get().length() > User.USERNAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_SIZE);
            }
            if (!userRequest.username.get().matches(User.USERNAME_PATTERN)) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_FORMAT);
            }
        }
        String userId = super.getUserId(principal);
        User currentUser = userService.get(userId);
        User targetUser = userService.get(id);
        
        accessChecker.checkCanManageWorkspaceUser(currentUser, targetUser);
        
        return userMapper.toWithProjectsDTO(userService.editMyUser(currentUser, targetUser, 
            userRequest.username, userRequest.color, userRequest.capitalLetters, 
            userRequest.changePassword, userRequest.githubToken, userRequest.enabled));
    }

}
