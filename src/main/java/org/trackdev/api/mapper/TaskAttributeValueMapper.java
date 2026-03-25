package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.EnumValueEntryDTO;
import org.trackdev.api.dto.TaskAttributeValueDTO;
import org.trackdev.api.entity.AttributeType;
import org.trackdev.api.entity.EnumValueEntry;
import org.trackdev.api.entity.TaskAttributeValue;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskAttributeValueMapper {

    @Named("toDTO")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    @Mapping(target = "attributeAppliedBy", source = "attribute.appliedBy")
    @Mapping(target = "value", expression = "java(getEffectiveValue(entity))")
    @Mapping(target = "textValue", expression = "java(getEffectiveTextValue(entity))")
    @Mapping(target = "enumValues", expression = "java(getEnumValues(entity))")
    TaskAttributeValueDTO toDTO(TaskAttributeValue entity);

    @IterableMapping(qualifiedByName = "toDTO")
    List<TaskAttributeValueDTO> toDTOList(List<TaskAttributeValue> entities);

    default String getEffectiveValue(TaskAttributeValue entity) {
        if (entity.getAttribute() != null && entity.getAttribute().getType() == AttributeType.TEXT) {
            return entity.getTextValue();
        }
        return entity.getValue();
    }

    default String getEffectiveTextValue(TaskAttributeValue entity) {
        if (entity.getAttribute() != null && entity.getAttribute().getType() == AttributeType.NUMERIC_TEXT) {
            return entity.getTextValue();
        }
        return null;
    }

    default EnumValueEntryDTO[] getEnumValues(TaskAttributeValue entity) {
        if (entity.getAttribute() != null &&
            entity.getAttribute().getEnumRef() != null) {
            return entity.getAttribute().getEnumRef().getValues().stream()
                    .map(this::toEnumValueEntryDTO)
                    .toArray(EnumValueEntryDTO[]::new);
        }
        return null;
    }

    default EnumValueEntryDTO toEnumValueEntryDTO(EnumValueEntry entry) {
        EnumValueEntryDTO dto = new EnumValueEntryDTO();
        dto.setValue(entry.getValue());
        dto.setDescription(entry.getDescription());
        return dto;
    }
}
