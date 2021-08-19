package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.configuration.AuthorizationConfiguration;
import org.udg.trackdev.spring.configuration.CorsConfiguration;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.configuration.JWTAuthorizationFilter;
import org.udg.trackdev.spring.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// This class is used to process all the authentication related URLs
@RequestMapping(path = "/auth")
@RestController
public class AuthController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    CorsConfiguration corsConfiguration;

    @Autowired
    AuthorizationConfiguration authorizationConfiguration;

    @PostMapping(path="/login")
    @JsonView(PrivacyLevelViews.Private.class)
    public ResponseEntity<Map<String, Object>> login(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @Valid @RequestBody LoginT userBody) {

        User user = userService.matchPassword(userBody.username, userBody.password);
        String token = getJWTToken(user);

        String cookieTokenValue = Base64.getEncoder().withoutPadding().encodeToString(token.getBytes());

        String sameSite = "Lax";
        String requestOrigin = request.getHeader("Origin");
        if(corsConfiguration.isEnabled() && corsConfiguration.isAllowedOrigin(requestOrigin)) {
            sameSite = "None";
        }
        ResponseCookie cookie = ResponseCookie.from("trackdev_JWT", cookieTokenValue)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().body(Map.of("userdata",user,"token", token));
    }

    @PostMapping(path="/logout")
    @JsonView(PrivacyLevelViews.Private.class)
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                                     HttpServletResponse response) {

        Cookie cookie = new Cookie("trackdev_JWT", "");
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return okNoContent();
    }

    @GetMapping(path="/self")
    @JsonView(PrivacyLevelViews.Private.class)
    public User self(Principal principal) {

        String userId = super.getUserId(principal);
        return userService.get(userId);
    }

    @GetMapping(path = "/check")
    public ResponseEntity check(Principal principal) {
        super.checkLoggedIn(principal);

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
                .signWith(JWTAuthorizationFilter.KEY)
                .compact();

        return "Bearer " + token;
    }


    static class LoginT {
        @NotNull
        public String username;
        @NotNull
        public String password;
    }

}
