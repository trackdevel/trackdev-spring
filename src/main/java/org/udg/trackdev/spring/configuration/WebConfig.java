package org.udg.trackdev.spring.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    CorsConfiguration corsConfiguration;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if(corsConfiguration.isEnabled()) {
            registry.addMapping("/**")
                .allowedOrigins(corsConfiguration.getAllowedOrigin())
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")     
                .allowCredentials(true).maxAge(3600);
        }
    }
}