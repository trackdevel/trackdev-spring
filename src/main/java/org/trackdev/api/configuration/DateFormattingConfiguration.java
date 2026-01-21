package org.trackdev.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trackdev.api.serializer.JsonZonedDateTimeDeserializer;
import org.trackdev.api.serializer.JsonZonedDateTimeSerializer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configuration for date/time formatting across the application.
 * All dates are stored and transmitted in UTC with timezone information.
 * Uses ZonedDateTime as the standard date/time type.
 * 
 * This class provides a single source of truth for date formatting:
 * - Use ISO_FORMATTER for all API date serialization/deserialization
 * - Use APP_FORMATTER for display purposes
 */
@Configuration
public class DateFormattingConfiguration {

    /**
     * ISO 8601 format with timezone offset for API responses.
     * Example: 2024-01-21T14:30:00+00:00
     */
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    
    /**
     * Simple date format (date only, no time).
     * Example: 2024-01-21
     */
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * Legacy format for backward compatibility during migration.
     * @deprecated Use ISO_DATE_TIME_FORMAT instead
     */
    @Deprecated
    public static final String SIMPLE_LOCALDATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * Display format with timezone name.
     * Example: 21-01-2024 - 14:30:00 UTC
     */
    public static final String APP_DATE_FORMAT = "dd-MM-yyyy - HH:mm:ss z";
    
    /**
     * Single source of truth: ISO 8601 DateTimeFormatter with timezone offset.
     * Use this formatter for all API date serialization and deserialization.
     * Thread-safe and immutable.
     */
    public static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT);
    
    /**
     * Single source of truth: App display DateTimeFormatter.
     * Use this formatter for display purposes (e.g., error messages).
     * Thread-safe and immutable.
     */
    public static final DateTimeFormatter APP_FORMATTER = 
        DateTimeFormatter.ofPattern(APP_DATE_FORMAT);

    /**
     * Customizes the Jackson ObjectMapper to use custom ZonedDateTime serializers/deserializers.
     * This ensures all ZonedDateTime fields are handled consistently across the application.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.serializerByType(ZonedDateTime.class, new JsonZonedDateTimeSerializer());
            builder.deserializerByType(ZonedDateTime.class, new JsonZonedDateTimeDeserializer());
        };
    }

    /**
     * Jackson ObjectMapper configured with Java 8 time support and UTC timezone.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        return mapper;
    }

    /**
     * DateTimeFormatter bean for ISO 8601 format with timezone.
     * Returns the static ISO_FORMATTER constant.
     */
    @Bean(name = "isoDateTimeFormatter")
    public DateTimeFormatter isoDateTimeFormatter() {
        return ISO_FORMATTER;
    }

    /**
     * DateTimeFormatter bean for app-wide display usage.
     * Returns the static APP_FORMATTER constant.
     */
    @Bean(name = "appDateFormatter")
    public DateTimeFormatter appDateFormatter() {
        return APP_FORMATTER;
    }
}
