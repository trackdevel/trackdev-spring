package org.trackdev.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Set;

import org.trackdev.api.entity.Role;

/**
 * Created by imartin on 14/02/17.
 */
public class JsonRolesSerializer extends JsonSerializer<Set<Role>> {

  @Override
  public void serialize(Set<Role> roles, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeStartArray();
    for (Role role: roles)
      gen.writeString(role.getUserType().toString());
    gen.writeEndArray();
  }
}
