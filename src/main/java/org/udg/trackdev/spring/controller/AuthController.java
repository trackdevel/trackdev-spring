package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.configuration.AuthorizationConfiguration;
import org.udg.trackdev.spring.configuration.CookieManager;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.service.EmailSenderService;
import org.udg.trackdev.spring.service.UserService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "1. Authentication")
@RequestMapping(path = "/auth")
@RestController
public class AuthController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    CookieManager cookieManager;

    @Autowired
    AuthorizationConfiguration authorizationConfiguration;

    @Autowired
    EmailSenderService emailSenderService;

    @Operation(summary = "Login user", description = "Login user with username and password")
    @PostMapping(path="/login")
    @JsonView(PrivacyLevelViews.Private.class)
    public ResponseEntity<Map<String, Object>> login(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @Valid @RequestBody LoginT userBody) {

        User user = userService.matchPassword(userBody.email, userBody.password);
        String token = getJWTToken(user);

        String cookieTokenValue = Base64.getEncoder().withoutPadding().encodeToString(token.getBytes());
        cookieManager.addSessionCookie(request, response, "trackdev_JWT", cookieTokenValue);

        userService.setLastLogin(user);

        return ResponseEntity.ok().body(Map.of("userdata",user,"token", token));
    }

    @Operation(summary = "Logout user",
            description = "Logout user",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(path="/logout")
    @JsonView(PrivacyLevelViews.Private.class)
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                                     HttpServletResponse response) {

        cookieManager.removeCookie(request, response, "trackdev_JWT");
        return okNoContent();
    }

    @Operation(summary = "Return the logged user",
            description = "Return the public information of the logged user",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping(path="/self")
    @JsonView(PrivacyLevelViews.Private.class)
    public User self(Principal principal) {

        String userId = super.getUserId(principal);
        return userService.get(userId);
    }

    @Operation(summary = "Check if user is logged",
            description = "Check if the user is logged to the website",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping(path = "/check")
    public ResponseEntity check(Principal principal) {
        super.checkLoggedIn(principal);

        return okNoContent();
    }

    @Operation(summary = "Change user password",
            description = "Change the password of the user for a new one",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(path="/password")
    public ResponseEntity changePassword(Principal principal,
                                                     @Valid @RequestBody ChangePasswordT userBody) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        userService.matchPassword(user.getUsername(), userBody.oldPassword);
        userService.changePassword(user, userBody.newPassword);

        return okNoContent();
    }

    @Operation(summary = "Get recovery code", description = "Get recovery code for user")
    @PostMapping(path="/recovery")
    public ResponseEntity recoveryCode(@Valid @RequestBody RecoveryPasswordR userBody) throws MessagingException {
        User user = userService.getByEmail(userBody.email);
        if(user == null) {
            throw new ServiceException("User with this email does not exist");
        }
        String tempCode = userService.generateRecoveryCode(user);
        emailSenderService.sendRecoveryEmail(userBody.email, tempCode);
        return okNoContent();
    }

    @Operation(summary = "Check recovery code", description = "Check recovery code for user")
    @PostMapping(path="/recovery/{email}/check")
    public ResponseEntity checkRecoveryCode(@PathVariable("email") String email, @Valid @RequestBody CodeValidationR codeValidation) {
        User user = userService.getByEmail(email);
        if(user == null) {
            throw new ServiceException("User with this mail does not exist");
        }
        if(!userService.matchRecoveryCode(user, codeValidation.code)) {
            throw new ServiceException("Code does not match");
        }
        return okNoContent();
    }

    @Operation(summary = "Recovery password", description = "Recover password with recovery code")
    @PostMapping(path="/recovery/{email}")
    public ResponseEntity recoveryPassword(@PathVariable("email") String email,  @Valid @RequestBody RecoveryPasswordT userBody) {
        User user = userService.getByEmail(email);
        if(user == null) {
            throw new ServiceException("User with this email does not exist");
        }
        if(!userService.matchRecoveryCode(user,userBody.code)) {
            throw new ServiceException("Code does not match");
        }
        userService.changePassword(user, userBody.newPassword);
        userService.cleanRecoveryCode(user);
        return okNoContent();
    }

    private String getJWTToken(User user) {
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        int durationInMinutes = authorizationConfiguration.getTokenLifetimeInMinutes();
        int durationInMilliseconds = durationInMinutes * 60 * 1000;

        String token = Jwts
                .builder()
                .setId("trackdev_JWT")
                .setSubject(user.getId())
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + durationInMilliseconds))
                .signWith(authorizationConfiguration.getKey())
                .compact();

        return "Bearer " + token;
    }

    static class LoginT {
        @NotNull
        public String email;
        @NotNull
        public String password;
    }

    static class ChangePasswordT {
        @NotNull
        public String oldPassword;
        @NotNull
        public String newPassword;
    }

    static class RecoveryPasswordR {
        @NotBlank
        public String email;
    }

    static class CodeValidationR {
        @NotBlank
        @Size(min = User.RECOVERY_CODE_LENGTH, max = User.RECOVERY_CODE_LENGTH)
        public String code;
    }

    static class RecoveryPasswordT {
        @NotBlank
        @Size(min = User.RECOVERY_CODE_LENGTH, max = User.RECOVERY_CODE_LENGTH)
        public String code;
        @NotBlank
        public String newPassword;
    }

}
