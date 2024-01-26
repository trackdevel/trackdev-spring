package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.UserService;
import org.udg.trackdev.spring.utils.ErrorConstants;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// This class is used to manage users and sign up
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "2. Users")
@RequestMapping(path = "/users")
@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    /**
     * Returns the public profile of any user.
     * @param principal The current authenticated entity
     * @param id The email of the user to request.
     * @return The User identified by username
     */
    @Operation(summary = "Get user by id", description = "Get user by id")
    @GetMapping(path = "/uuid/{id}")
    @JsonView(EntityLevelViews.UserWithoutProjectMembers.class)
    public User getPublic(Principal principal, @PathVariable("id") String id) {
        super.checkLoggedIn(principal);
        return userService.get(id);
    }

    /**
     * Returns the public profile of any user.
     * @param principal The current authenticated entity
     * @param email The email of the user to request.
     * @return The User identified by username
     */
    @Operation(summary = "Get user by email", description = "Get user by email")
    @GetMapping(path = "/{email}")
    @JsonView(EntityLevelViews.UserWithoutProjectMembers.class)
    public User getUserEmail(Principal principal, @PathVariable("email") String email) {
        super.checkLoggedIn(principal);
        return userService.getByEmail(email);
    }

    @Operation(summary = "Get all users", description = "Get all users, only admins can do this")
    @GetMapping
    @JsonView({EntityLevelViews.UserWithoutProjectMembers.class})
    public List<User> getAll(Principal principal) {
        if (!accessChecker.isUserAdmin(userService.get(principal.getName()))){
            throw new SecurityException(ErrorConstants.UNAUTHORIZED);
        }
        return userService.findAll();
    }

    @Operation(summary = "Register user", description = "Register user, only admins can do this")
    @PostMapping(path = "/register")
    public ResponseEntity<Void> register(Principal principal, @Valid @RequestBody RegisterU ru,
                                         BindingResult result) {
        checkLoggedIn(principal);
        if (!accessChecker.isUserAdmin(userService.get(principal.getName()))) {
            throw new SecurityException(ErrorConstants.UNAUTHORIZED);
        }
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        userService.register(ru.username, ru.email);
        return okNoContent();
    }

    @Operation(summary = "Edit your user", description = "Edit your user")
    @PatchMapping
    @JsonView({EntityLevelViews.UserWithGithubToken.class})
    public User editMyUser(Principal principal,
                         @Valid @RequestBody EditU userRequest) {
        if (userRequest.username != null){
            if (userRequest.username.get().isEmpty() || userRequest.username.get().length() > User.USERNAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_SIZE);
            }
            if (!userRequest.username.get().matches("^[a-zA-ZÀ-ÖØ-öø-ÿ ]+$")) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_FORMAT);
            }
        }
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        return userService.editMyUser(user, user, userRequest.username, userRequest.color,
                userRequest.capitalLetters, userRequest.changePassword, userRequest.githubToken, userRequest.enabled);
    }

    @Operation(summary = "Edit user", description = "Edit user, only admins can do this")
    @PatchMapping(path = "/{id}")
    @JsonView({EntityLevelViews.UserWithoutProjectMembers.class})
    public User editUser(Principal principal,
                            @Valid @RequestBody EditU userRequest,
                            @PathVariable("id") String id) {
        if (userRequest.username != null){
            if (userRequest.username.get().isEmpty() || userRequest.username.get().length() > User.USERNAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_SIZE);
            }
            if (!userRequest.username.get().matches("^[a-zA-ZÀ-ÖØ-öø-ÿ ]+$")) {
                throw new ControllerException(ErrorConstants.INVALID_USERNAME_FORMAT);
            }
        }
        String userId = super.getUserId(principal);
        User modifier = userService.get(userId);
        if (!accessChecker.isUserAdmin(modifier)){
            throw new SecurityException(ErrorConstants.UNAUTHORIZED);
        }
        else {
            User user = userService.get(id);
            return userService.editMyUser(modifier, user, userRequest.username, userRequest.color,
                    userRequest.capitalLetters, userRequest.changePassword, userRequest.githubToken, userRequest.enabled);
        }
    }

    @Operation(summary = "Check if autenticated user is admin", description = "Check if autenticated user is admin")
    @GetMapping(path = "/checker/admin")
    public ResponseEntity<Void> imAdminUser(Principal principal) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        if (accessChecker.isUserAdmin(user)) {
            return okNoContent();
        } else {
            throw new SecurityException(ErrorConstants.UNAUTHORIZED);
        }
    }

    static class RegisterU {
        @NotBlank
        @Size(
                min = User.MIN_USERNAME_LENGTH,
                max = User.USERNAME_LENGTH,
                message = ErrorConstants.INVALID_USERNAME_SIZE
        )
        @Pattern(
                regexp = "^[a-zA-ZÀ-ÖØ-öø-ÿ ]+$",
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

    }

    static class EditU {

        public Optional<String> username;

        public Optional<String> color;

        public Optional<String> capitalLetters;

        public Optional<Boolean> changePassword;

        public Optional<String> githubToken;

        public Optional<Boolean> enabled;
    }

}
