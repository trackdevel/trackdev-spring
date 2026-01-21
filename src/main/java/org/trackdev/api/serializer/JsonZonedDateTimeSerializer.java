package org.trackdev.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.trackdev.api.configuration.DateFormattingConfiguration;

/**
 * Custom JSON serializer for ZonedDateTime.
 * Serializes ZonedDateTime to ISO 8601 format with timezone offset.
 * Uses the centralized ISO_FORMATTER from DateFormattingConfiguration.
 * Example output: "2024-01-21T14:30:00+00:00"
 */
public class JsonZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

    @Override
    public void serialize(ZonedDateTime dateTime, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (dateTime == null) {
            gen.writeNull();
        } else {
            gen.writeString(dateTime.format(DateFormattingConfiguration.ISO_FORMATTER));
        }
    }
}
