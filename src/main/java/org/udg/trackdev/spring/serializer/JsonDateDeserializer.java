package org.udg.trackdev.spring.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.udg.trackdev.spring.service.Global;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by imartin on 14/02/17.
 */
public class JsonDateDeserializer extends JsonDeserializer<Date> {

  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    try {
      return Global.dateFormat.parse(jsonParser.getValueAsString());
    } catch (ParseException e) {
      return null;
    }
  }
}
