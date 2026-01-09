package org.trackdev.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.trackdev.api.configuration.DateFormattingConfiguration;

/**
 * Created by imartin on 14/02/17.
 */
public class JsonDateSerializer extends JsonSerializer<Date> {

  @Override
  public void serialize(Date date, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    // Create new SimpleDateFormat instance for thread safety
    SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormattingConfiguration.SIMPLE_DATE_FORMAT);
    String formattedDate = dateFormat.format(date);
    gen.writeString(formattedDate);
  }
}
