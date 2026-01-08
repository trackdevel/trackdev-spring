package org.trackdev.api.configuration;
// package org.udg.trackdev.spring.configuration;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// import static org.springframework.security.config.Customizer.withDefaults;

// @EnableWebSecurity
// @Configuration
// public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

//     @Autowired
//     private AuthorizationConfiguration authorizationConfiguration;

//     @Autowired
//     private CookieManager cookieManager;

//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         http.csrf(csrf -> csrf.disable())
//                 .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                 .cors(withDefaults())
//                 .addFilterAfter(new JWTAuthorizationFilter(authorizationConfiguration, cookieManager), UsernamePasswordAuthenticationFilter.class)
//                 .authorizeHttpRequests(requests -> requests
//                         .antMatchers(HttpMethod.POST, "/auth/login", "/auth/recovery/**").permitAll()
//                         .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
//                         .anyRequest().authenticated());

//         http.headers(headers -> headers
//                 .httpStrictTransportSecurity(security -> security.disable()));
//     }
// }
