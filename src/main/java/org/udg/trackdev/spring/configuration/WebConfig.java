package org.udg.trackdev.spring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${trackdev.cors.allowed-origin:#{null}}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        if(allowedOrigin != null) {
            registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                .allowCredentials(true).maxAge(3600);
        }
    }
}