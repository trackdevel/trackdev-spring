package org.trackdev.api.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.trackdev.api.configuration.DateFormattingConfiguration;

/**
 * Custom JSON deserializer for ZonedDateTime.
 * Accepts ISO 8601 format with timezone offset.
 * Uses the centralized ISO_FORMATTER from DateFormattingConfiguration.
 * All dates without timezone are assumed to be UTC.
 * Example input: "2024-01-21T14:30:00+00:00" or "2024-01-21T14:30:00Z"
 */
public class JsonZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext context) 
            throws IOException {
        String value = jsonParser.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing with the standard format first
            return ZonedDateTime.parse(value, DateFormattingConfiguration.ISO_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Try ISO format (handles 'Z' suffix)
                return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException e2) {
                try {
                    // Try ISO offset format
                    return ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (DateTimeParseException e3) {
                    try {
                        // Try ISO local datetime and assume UTC
                        return ZonedDateTime.parse(value + "Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    } catch (DateTimeParseException e4) {
                        // Last resort: try parsing as local datetime and assume UTC
                        try {
                            return java.time.LocalDateTime.parse(value, 
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                .atZone(ZoneId.of("UTC"));
                        } catch (DateTimeParseException e5) {
                            throw new IOException("Unable to parse date: " + value, e5);
                        }
                    }
                }
            }
        }
    }
}
