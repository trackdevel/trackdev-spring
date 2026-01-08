package org.trackdev.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import org.trackdev.api.model.ErrorEntity;
import org.trackdev.api.service.Global;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    public static final String HEADER = "Authorization";
    public static final String PREFIX = "Bearer ";

    private final AuthorizationConfiguration authorizationConfiguration;
    private final CookieManager cookieManager;

    public JWTAuthorizationFilter(AuthorizationConfiguration authorizationConfiguration, CookieManager cookieManager) {
        this.authorizationConfiguration = authorizationConfiguration;
        this.cookieManager = cookieManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            Claims claims = validateToken(request);
            if (claims != null) {
                if (claims.get("authorities") != null) {
                    setUpSpringAuthentication(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
            chain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            // For public endpoints, ignore JWT errors and proceed without authentication
            if (isPublicEndpoint(request)) {
                SecurityContextHolder.clearContext();
                cookieManager.removeCookie(request, response, "trackdev_JWT");
                chain.doFilter(request, response);
                return;
            }
            
            cookieManager.removeCookie(request, response, "trackdev_JWT");

            ErrorEntity errorEntityResponse = new ErrorEntity(Global.dateFormat.format(new Date()), HttpStatus.FORBIDDEN.value(), "Security error", e.getMessage());

            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(new ObjectMapper().writeValueAsString(errorEntityResponse));
            out.flush();
        }
    }

    /**
     * Check if the request is for a public endpoint that doesn't require authentication.
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        // Auth endpoints
        if (path.equals("/auth/login") || path.startsWith("/auth/recovery")) {
            return true;
        }
        
        // Invite endpoints
        if (path.startsWith("/invites")) {
            return true;
        }
        
        // Webhook endpoints
        if (path.startsWith("/hooks")) {
            return true;
        }
        
        // Swagger/OpenAPI endpoints
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-resources")) {
            return true;
        }
        
        return false;
    }

    private Claims validateToken(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HEADER);
        if (authenticationHeader != null) {
            if (authenticationHeader.startsWith(PREFIX)) {
                String token = authenticationHeader.replace(PREFIX, "");
                return Jwts.parser()
                           .verifyWith(authorizationConfiguration.getKey())
                           .build()
                           .parseSignedClaims(token)
                           .getPayload();
            } else {
                return null;
            }
        } else {
            Cookie tokenCookie = WebUtils.getCookie(request, "trackdev_JWT");
            if (tokenCookie != null) {
                String cv = tokenCookie.getValue();
                String decoded = new String(Base64.getDecoder().decode(cv));
                String token = decoded.replace(PREFIX, "");
                return Jwts.parser()
                           .verifyWith(authorizationConfiguration.getKey())
                           .build()
                           .parseSignedClaims(token)
                           .getPayload();
            } else {
                return null;
            }
        }
    }

    /**
     * Metodo para autenticarnos dentro del flujo de Spring
     *
     * @param claims
     */
    @SuppressWarnings("unchecked")
    private void setUpSpringAuthentication(Claims claims) {
        List<String> authorities = (List<String>) claims.get("authorities");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}