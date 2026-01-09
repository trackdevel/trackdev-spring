package org.trackdev.api.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.trackdev.api.configuration.DateFormattingConfiguration;

/**
 * Created by imartin on 14/02/17.
 */
public class JsonDateDeserializer extends JsonDeserializer<Date> {

  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    try {
      // Create new SimpleDateFormat instance for thread safety
      SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormattingConfiguration.SIMPLE_DATE_FORMAT);
      return dateFormat.parse(jsonParser.getValueAsString());
    } catch (ParseException e) {
      return null;
    }
  }
}
