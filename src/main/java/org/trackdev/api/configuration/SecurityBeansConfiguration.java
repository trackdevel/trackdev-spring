package org.trackdev.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

@Configuration
public class SecurityBeansConfiguration {

    /**
     * Primary password encoder using SCrypt (more secure than BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * BCrypt encoder for legacy support if needed
     */
    @Bean(name = "bCryptPasswordEncoder")
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
