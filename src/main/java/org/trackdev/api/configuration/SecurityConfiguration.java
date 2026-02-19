package org.trackdev.api.configuration;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

/**
 * Spring Security Configuration
 * 
 * Security features:
 * - Stateless session management (JWT-based authentication)
 * - CSRF disabled (safe for stateless APIs with proper CORS)
 * - Method-level security enabled (@PreAuthorize, @Secured annotations)
 * - JWT filter for token validation
 * - JWT token refresh filter for sliding session expiration
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration {

    @Autowired
    private AuthorizationConfiguration authorizationConfiguration;

    @Autowired
    private CookieManager cookieManager;

    @Autowired
    private JWTTokenRefreshFilter jwtTokenRefreshFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(management -> management
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(withDefaults())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/recovery/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/forgot-password", "/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/reset-password/validate").permitAll()
                .requestMatchers(HttpMethod.GET, "/invites/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/discord/callback").permitAll()
                .requestMatchers(HttpMethod.POST, "/discord/interactions").permitAll()
                .requestMatchers(HttpMethod.POST, "/invites/*/accept").permitAll()
                .requestMatchers("/hooks/**").permitAll()  // GitHub webhooks (unauthenticated, validated via signature)
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                new JWTAuthorizationFilter(authorizationConfiguration, cookieManager),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtTokenRefreshFilter, JWTAuthorizationFilter.class);

        return http.build();
    }

}
