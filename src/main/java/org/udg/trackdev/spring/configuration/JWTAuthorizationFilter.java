package org.udg.trackdev.spring.configuration;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.web.util.WebUtils;
import org.udg.trackdev.spring.service.Global;
import org.udg.trackdev.spring.model.ErrorEntity;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    public static final String HEADER = "Authorization";
    public static final String PREFIX = "Bearer ";

    private AuthorizationConfiguration authorizationConfiguration;
    private CookieManager cookieManager;

    public JWTAuthorizationFilter(AuthorizationConfiguration authorizationConfiguration, CookieManager cookieManager) {
        this.authorizationConfiguration = authorizationConfiguration;
        this.cookieManager = cookieManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            Jws<Claims> claims = validateToken(request);
            if (claims != null) {
                if (claims.getBody().get("authorities") != null) {
                    setUpSpringAuthentication(claims.getBody());
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
            chain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
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

    private Jws<Claims> validateToken(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HEADER);
        if (authenticationHeader != null) {
            if (authenticationHeader.startsWith(PREFIX)) {
                return Jwts.parserBuilder()
                           .setSigningKey(authorizationConfiguration.getKey())
                           .build()
                           .parseClaimsJws(authenticationHeader.replace(PREFIX, ""));
            } else
                return null;
        } else {
            Cookie tokenCookie = WebUtils.getCookie(request, "trackdev_JWT");
            if (tokenCookie != null) {
                String cv = tokenCookie.getValue();
                String decoded = new String(Base64.getDecoder().decode(cv));
                return Jwts.parserBuilder()
                                  .setSigningKey(authorizationConfiguration.getKey())
                                  .build()
                                  .parseClaimsJws(decoded.replace(PREFIX, ""));
            } else
                return null;
        }
    }


    /**
     * Metodo para autenticarnos dentro del flujo de Spring
     *
     * @param claims
     */
    private void setUpSpringAuthentication(Claims claims) {
        List<String> authorities = (List<String>) claims.get("authorities");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);

    }
}