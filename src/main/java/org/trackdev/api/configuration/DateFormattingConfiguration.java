package org.trackdev.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * Configuration for date/time formatting across the application.
 * Provides consistent date formatters as Spring beans.
 */
@Configuration
public class DateFormattingConfiguration {

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String SIMPLE_LOCALDATE_FORMAT = "yyyy-MM-dd";
    public static final String APP_DATE_FORMAT = "dd-MM-yyyy - HH:mm:ss z";

    /**
     * Jackson ObjectMapper configured with consistent date formatting
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(SIMPLE_DATE_FORMAT));
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    /**
     * DateTimeFormatter for app-wide usage
     */
    @Bean(name = "appDateFormatter")
    public DateTimeFormatter appDateFormatter() {
        return DateTimeFormatter.ofPattern(APP_DATE_FORMAT);
    }

    /**
     * SimpleDateFormat bean - Note: SimpleDateFormat is not thread-safe,
     * so each usage should get a new instance or use synchronized access
     */
    @Bean
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat(SIMPLE_DATE_FORMAT);
    }
}
