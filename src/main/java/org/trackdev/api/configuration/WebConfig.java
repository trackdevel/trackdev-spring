package org.trackdev.api.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
// Note: Do NOT use @EnableWebMvc here - it disables Spring Boot's auto-configuration
// including Jackson's Jdk8Module needed for Optional<T> in request bodies
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    CorsConfiguration corsConfiguration;

    @Autowired
    TrackDevProperties trackDevProperties;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    /**
     * Re-registers OSIV (OpenEntityManagerInView) manually with SSE endpoint exclusion.
     * Spring Boot's auto-configured OSIV is disabled via spring.jpa.open-in-view=false.
     *
     * OSIV keeps the Hibernate EntityManager (and its DB connection) open for the entire
     * HTTP request lifecycle. For normal REST requests this is fine (milliseconds), but for
     * SSE endpoints the request lives for the emitter lifetime (up to 30 minutes), holding
     * a DB connection the entire time. With 80+ concurrent SSE connections this exhausts
     * the HikariCP pool, blocking all REST requests.
     *
     * By excluding SSE paths, those endpoints get no OSIV EntityManager — the auth check
     * in checkSprintAccess() uses its own @Transactional(propagation=REQUIRES_NEW) which
     * creates and closes a dedicated EntityManager, releasing the DB connection immediately.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        OpenEntityManagerInViewInterceptor osiv = new OpenEntityManagerInViewInterceptor();
        osiv.setEntityManagerFactory(entityManagerFactory);
        registry.addWebRequestInterceptor(osiv)
                .excludePathPatterns("/sprints/*/events");
    }

    /**
     * Configures a dedicated, bounded thread pool for async request processing (SSE).
     * This keeps SSE connections completely isolated from Tomcat's request thread pool,
     * ensuring REST API requests are never starved by long-lived SSE connections.
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(trackDevProperties.getSse().getAsyncPoolSize());
        executor.setMaxPoolSize(trackDevProperties.getSse().getAsyncPoolSize());
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("sse-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        configurer.setTaskExecutor(executor);
        configurer.setDefaultTimeout(trackDevProperties.getSse().getEmitterTimeoutMs());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if(corsConfiguration.isEnabled()) {
            registry.addMapping("/**")
                .allowedOrigins(corsConfiguration.getAllowedOrigin())
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .exposedHeaders("X-Refreshed-Token")
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