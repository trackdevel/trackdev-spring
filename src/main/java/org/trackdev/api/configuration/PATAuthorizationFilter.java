package org.trackdev.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.trackdev.api.entity.PersonalAccessToken;
import org.trackdev.api.entity.User;
import org.trackdev.api.model.ErrorEntity;
import org.trackdev.api.service.PersonalAccessTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class PATAuthorizationFilter extends OncePerRequestFilter {

    public static final String HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String PAT_PREFIX = PersonalAccessTokenService.TOKEN_PREFIX;
    public static final String PAT_AUTH_ATTRIBUTE = "PAT_AUTHENTICATED";

    private final PersonalAccessTokenService patService;

    public PATAuthorizationFilter(PersonalAccessTokenService patService) {
        this.patService = patService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            if (token.startsWith(PAT_PREFIX)) {
                try {
                    PersonalAccessToken pat = patService.authenticate(token);
                    if (pat != null) {
                        User user = pat.getUser();
                        var authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getUserType().name()))
                            .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                user.getId(), null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        request.setAttribute(PAT_AUTH_ATTRIBUTE, true);
                    } else {
                        SecurityContextHolder.clearContext();
                        sendErrorResponse(response, "Invalid or expired personal access token");
                        return;
                    }
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    sendErrorResponse(response, "PAT authentication error");
                    return;
                }

                chain.doFilter(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message)
            throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            DateFormattingConfiguration.SIMPLE_DATE_FORMAT);
        ErrorEntity errorEntity = new ErrorEntity(
            dateFormat.format(new Date()),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication error",
            message);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(new ObjectMapper().writeValueAsString(errorEntity));
        out.flush();
    }
}
