package org.trackdev.api.controller;

import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.configuration.AuthorizationConfiguration;
import org.trackdev.api.configuration.CookieManager;
import org.trackdev.api.configuration.RateLimiter;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.LoginResponseDTO;
import org.trackdev.api.dto.UserWithGithubTokenDTO;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.UserMapper;
import org.trackdev.api.service.EmailSenderService;
import org.trackdev.api.service.SecurityAuditLogger;
import org.trackdev.api.service.UserService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "1. Authentication")
@RequestMapping(path = "/auth")
@RestController
public class AuthController extends BaseController {

    private static final String COOKIE_NAME = "trackdev_JWT";

    @Autowired
    UserService userService;

    @Autowired
    CookieManager cookieManager;

    @Autowired
    AuthorizationConfiguration authorizationConfiguration;

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    RateLimiter rateLimiter;

    @Autowired
    SecurityAuditLogger auditLogger;

    @Autowired
    UserMapper userMapper;

    @Operation(summary = "Login user", description = "Login user with username and password")
    @PostMapping(path="/login")
    public LoginResponseDTO login(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @Valid @RequestBody LoginT userBody) {
        // Rate limiting by IP address and email
        String clientIp = getClientIpAddress(request);
        String rateLimitKey = clientIp + ":" + userBody.email;
        
        if (!rateLimiter.isAllowed(rateLimitKey)) {
            long remaining = rateLimiter.getRemainingLockoutSeconds(rateLimitKey);
            auditLogger.logRateLimited(userBody.email, clientIp);
            throw new ControllerException("Too many login attempts. Please try again in " + remaining + " seconds.");
        }
        
        try {
            User user = userService.matchPassword(userBody.email, userBody.password);
            String token = getJWTToken(user);

            String cookieTokenValue = Base64.getEncoder().withoutPadding().encodeToString(token.getBytes());
            cookieManager.addSessionCookie(request, response, COOKIE_NAME, cookieTokenValue);

            userService.setLastLogin(user);
            
            // Clear rate limit on successful login
            rateLimiter.recordSuccess(rateLimitKey);
            
            // Audit log successful login
            auditLogger.logLoginSuccess(userBody.email, clientIp);

            return new LoginResponseDTO(userMapper.toWithGithubTokenDTO(user), token);
        } catch (Exception e) {
            // Audit log failed login
            auditLogger.logLoginFailure(userBody.email, clientIp, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extract client IP address, handling proxies (X-Forwarded-For header).
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "Logout user",
            description = "Logout user",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(path="/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                                     HttpServletResponse response) {

        cookieManager.removeCookie(request, response, COOKIE_NAME);
        return okNoContent();
    }

    @Operation(summary = "Return the logged user",
            description = "Return the public information of the logged user",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping(path="/self")
    public UserWithGithubTokenDTO self(Principal principal) {

        String userId = super.getUserId(principal);
        return userMapper.toWithGithubTokenDTO(userService.get(userId));
    }

    @Operation(summary = "Check if user is logged",
            description = "Check if the user is logged to the website",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping(path = "/check")
    public ResponseEntity<Void> check(Principal principal) {
        super.checkLoggedIn(principal);

        return okNoContent();
    }

    @Operation(summary = "Change user password",
            description = "Change the password of the user for a new one",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(path="/password")
    public ResponseEntity<Void> changePassword(Principal principal,
                                               @Valid @RequestBody ChangePasswordT userBody,
                                               BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        userService.changePasswordWithVerification(userId, userBody.oldPassword, userBody.newPassword);

        return okNoContent();
    }

    @Operation(summary = "Get recovery code", description = "Get recovery code for user")
    @PostMapping(path="/recovery")
    public ResponseEntity<Void> recoveryCode(@Valid @RequestBody RecoveryPasswordR userBody) {
        User user = userService.getByEmail(userBody.email);
        if(user == null) {
            throw new ControllerException(ErrorConstants.USER_MAIL_NOT_FOUND);
        }
        String tempCode = userService.generateRecoveryCode(user);
        emailSenderService.sendRecoveryEmail(userBody.email, tempCode, "en");
        return okNoContent();
    }

    @Operation(summary = "Check recovery code", description = "Check recovery code for user")
    @PostMapping(path="/recovery/{email}/check")
    public ResponseEntity<Void> checkRecoveryCode(@PathVariable String email, @Valid @RequestBody CodeValidationR codeValidation) {
        User user = userService.getByEmail(email);
        if(user == null) {
            throw new ControllerException(ErrorConstants.USER_MAIL_NOT_FOUND);
        }
        if(!userService.matchRecoveryCode(user, codeValidation.code)) {
            throw new ControllerException(ErrorConstants.RECOVERY_CODE_NOT_MATCH);
        }
        return okNoContent();
    }

    @Operation(summary = "Recovery password", description = "Recover password with recovery code")
    @PostMapping(path="/recovery/{email}")
    public ResponseEntity<Void> recoveryPassword(@PathVariable String email,  @Valid @RequestBody RecoveryPasswordT userBody, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        // All operations in a single transaction
        userService.recoverPassword(email, userBody.code, userBody.newPassword);
        return okNoContent();
    }

    /**
     * Generate JWT token with user's actual roles from database.
     * Roles are prefixed with "ROLE_" for Spring Security compatibility.
     * This enables @PreAuthorize("hasRole('ADMIN')") annotations to work.
     */
    private String getJWTToken(User user) {
        // Get actual user roles from the database
        List<String> userRoles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getUserType().name())
                .collect(Collectors.toList());

        int durationInMinutes = authorizationConfiguration.getTokenLifetimeInMinutes();
        int durationInMilliseconds = durationInMinutes * 60 * 1000;

        String token = Jwts
                .builder()
                .setId(COOKIE_NAME)
                .setSubject(user.getId())
                .claim("authorities", userRoles)
                .claim("email", user.getEmail())  // Include email for audit purposes
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
        @NotBlank
        @Size(
                min = 8,
                message = ErrorConstants.PASSWORD_MINIUM_LENGTH
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = ErrorConstants.INVALID_PASSWORD_FORMAT
        )
        public String newPassword;
    }

    static class RecoveryPasswordR {
        @NotBlank
        public String email;
    }

    static class CodeValidationR {

        public String code;
    }

    static class RecoveryPasswordT {

        public String code;
        @NotBlank
        @Size(
                min = 8,
                message = ErrorConstants.PASSWORD_MINIUM_LENGTH
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = ErrorConstants.INVALID_PASSWORD_FORMAT
        )
        public String newPassword;
    }

}
