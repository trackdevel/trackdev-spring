package org.trackdev.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for handling Java 8 types like Optional.
 * Spring Boot should auto-configure this, but we explicitly ensure Jdk8Module is registered.
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        // Explicitly register Jdk8Module to handle Optional<T> in request/response
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }
}
