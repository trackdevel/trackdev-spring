package org.trackdev.api.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
// Note: Do NOT use @EnableWebMvc here - it disables Spring Boot's auto-configuration
// including Jackson's Jdk8Module needed for Optional<T> in request bodies
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

    /**
     * Configures the LocaleResolver to read the locale from the Accept-Language HTTP header.
     * Supports English (default), Catalan (ca), and Spanish (es).
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setSupportedLocales(List.of(
                Locale.ENGLISH,
                Locale.forLanguageTag("ca"),    // Catalan
                Locale.forLanguageTag("es")     // Spanish
        ));
        return localeResolver;
    }
}