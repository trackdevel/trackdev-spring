package org.trackdev.api.configuration;

import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.trackdev.api.entity.User;
import org.trackdev.api.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter that refreshes JWT tokens on each authenticated request.
 * This implements a "sliding session" mechanism where active users
 * have their token expiration extended automatically.
 * 
 * The refreshed token is returned in:
 * - X-Refreshed-Token header (for API clients)
 * - Updated trackdev_JWT cookie (for web clients)
 */
@Component
public class JWTTokenRefreshFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "trackdev_JWT";
    private static final String REFRESHED_TOKEN_HEADER = "X-Refreshed-Token";
    
    private final AuthorizationConfiguration authorizationConfiguration;
    private final CookieManager cookieManager;
    private final UserService userService;

    public JWTTokenRefreshFilter(
            AuthorizationConfiguration authorizationConfiguration,
            CookieManager cookieManager,
            UserService userService) {
        this.authorizationConfiguration = authorizationConfiguration;
        this.cookieManager = cookieManager;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // Continue with the filter chain first
        filterChain.doFilter(request, response);
        
        // After the request is processed, check if user is authenticated
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && authentication.getPrincipal() instanceof String) {
                
                String userId = (String) authentication.getPrincipal();
                
                // Skip token refresh for certain endpoints
                String requestPath = request.getRequestURI();
                if (shouldSkipRefresh(requestPath)) {
                    return;
                }
                
                // Generate refreshed token
                String refreshedToken = generateRefreshedToken(userId);
                
                // Add refreshed token to response header
                response.setHeader(REFRESHED_TOKEN_HEADER, refreshedToken);
                
                // Also update the cookie for web clients
                String cookieTokenValue = Base64.getEncoder()
                        .withoutPadding()
                        .encodeToString(refreshedToken.getBytes());
                cookieManager.addSessionCookie(request, response, COOKIE_NAME, cookieTokenValue);
            }
        } catch (Exception e) {
            // Don't fail the request if token refresh fails
            // Just log and continue - the original token is still valid
            logger.debug("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Skip token refresh for certain endpoints to avoid unnecessary overhead.
     */
    private boolean shouldSkipRefresh(String requestPath) {
        return requestPath.startsWith("/auth/logout") 
            || requestPath.startsWith("/auth/login")
            || requestPath.startsWith("/auth/recovery")
            || requestPath.startsWith("/swagger")
            || requestPath.startsWith("/v3/api-docs");
    }
    
    /**
     * Generate a new JWT token with refreshed expiration time.
     */
    private String generateRefreshedToken(String userId) {
        User user = userService.get(userId);
        
        // Get user roles
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
                .claim("email", user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + durationInMilliseconds))
                .signWith(authorizationConfiguration.getKey())
                .compact();

        return "Bearer " + token;
    }
}
