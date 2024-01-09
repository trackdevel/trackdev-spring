package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
            throw new SecurityException("Only admins can list all users");
        }
        return userService.findAll();
    }

    @Operation(summary = "Register user", description = "Register user, only admins can do this")
    @PostMapping(path = "/register")
    public ResponseEntity<Void> register(Principal principal, @Valid @RequestBody RegisterU ru) {
        checkLoggedIn(principal);
        if (!accessChecker.isUserAdmin(userService.get(principal.getName()))) {
            throw new SecurityException("Only admins can register users");
        }
        userService.register(ru.username, ru.email);
        return okNoContent();
    }

    @Operation(summary = "Edit your user", description = "Edit your user")
    @PatchMapping
    @JsonView({EntityLevelViews.UserWithGithubToken.class})
    public User editMyUser(Principal principal,
                         @Valid @RequestBody EditU userRequest) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        return userService.editMyUser(user, user, userRequest.username, userRequest.color,
                userRequest.capitalLetters, userRequest.changePassword, userRequest.githubToken);
    }

    @Operation(summary = "Edit user", description = "Edit user, only admins can do this")
    @PatchMapping(path = "/{id}")
    @JsonView({EntityLevelViews.UserWithoutProjectMembers.class})
    public User editUser(Principal principal,
                            @Valid @RequestBody EditU userRequest,
                            @PathVariable("id") String id) {
        String userId = super.getUserId(principal);
        User modifier = userService.get(userId);
        if (!accessChecker.isUserAdmin(modifier)){
            throw new SecurityException("You are not authorized");
        }
        else {
            User user = userService.get(id);
            return userService.editMyUser(modifier, user, userRequest.username, userRequest.color,
                    userRequest.capitalLetters, userRequest.changePassword, userRequest.githubToken);
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
            throw new SecurityException("You are not admin");
        }
    }

    static class RegisterU {
        @NotBlank
        @Size(min = 4, max = User.USERNAME_LENGTH)
        @Pattern(regexp = "^[a-zA-Z ]+$")
        public String username;

        @NotBlank
        @Email
        @Size(max = User.EMAIL_LENGTH)
        public String email;

    }

    static class EditU {

        public Optional<String> username;

        public Optional<String> color;

        public Optional<String> capitalLetters;

        public Optional<Boolean> changePassword;

        public Optional<String> githubToken;
    }

}
