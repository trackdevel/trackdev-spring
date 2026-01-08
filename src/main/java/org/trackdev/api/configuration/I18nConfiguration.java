package org.trackdev.api.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configuration for internationalization (i18n) support.
 * Configures MessageSource for loading translations.
 * LocaleResolver is configured in WebConfig.
 */
@Configuration
public class I18nConfiguration {

    /**
     * Configures the MessageSource to load messages from properties files.
     * Files are expected at: messages.properties, messages_ca.properties, messages_es.properties, etc.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Refresh cache every hour
        messageSource.setFallbackToSystemLocale(false); // Fall back to messages.properties, not system locale
        return messageSource;
    }
}
