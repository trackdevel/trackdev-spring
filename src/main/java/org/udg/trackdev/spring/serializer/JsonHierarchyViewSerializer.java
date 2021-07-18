package org.udg.trackdev.spring.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import java.io.IOException;

public class JsonHierarchyViewSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object object, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) gen.getCodec();
        ObjectWriter writer = mapper.writerWithView(EntityLevelViews.Hierarchy.class);
        String serializedObject = writer.writeValueAsString(object);
        gen.writeRawValue(serializedObject);
    }
}
