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
    public UserWithProjectsDTO getPublic(Principal principal, @PathVariable String id) {
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
    public UserWithProjectsDTO getUserEmail(Principal principal, @PathVariable String email) {
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

    @Operation(summary = "Register user", description = "Register user, only admins can do this")
    @PostMapping(path = "/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> register(Principal principal, @Valid @RequestBody RegisterU ru,
                                         BindingResult result) {
        checkLoggedIn(principal);
        // Double-check with AccessChecker for defense in depth
        if (!accessChecker.isUserAdmin(userService.get(principal.getName()))) {
            throw new SecurityException(ErrorConstants.UNAUTHORIZED);
        }
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        userService.register(ru.username, ru.email, ru.password, ru.userType);
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
     * Delete a user. Only admins can delete users.
     * User can only be deleted if they have no dependencies.
     */
    @Operation(summary = "Delete user", description = "Delete a user if they have no dependencies")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(Principal principal, @PathVariable String id) {
        String userId = getUserId(principal);
        User currentUser = userService.get(userId);
        accessChecker.checkIsUserAdmin(currentUser);
        
        userService.deleteUser(id);
        return okNoContent();
    }

}
