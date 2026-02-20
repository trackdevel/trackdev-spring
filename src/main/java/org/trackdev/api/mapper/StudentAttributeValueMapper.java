package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.StudentAttributeValueDTO;
import org.trackdev.api.entity.EnumValueEntry;
import org.trackdev.api.entity.StudentAttributeValue;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentAttributeValueMapper {

    @Named("toDTO")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    @Mapping(target = "attributeAppliedBy", source = "attribute.appliedBy")
    @Mapping(target = "enumValues", expression = "java(getEnumValues(entity))")
    StudentAttributeValueDTO toDTO(StudentAttributeValue entity);

    @IterableMapping(qualifiedByName = "toDTO")
    List<StudentAttributeValueDTO> toDTOList(List<StudentAttributeValue> entities);

    default String[] getEnumValues(StudentAttributeValue entity) {
        if (entity.getAttribute() != null &&
            entity.getAttribute().getEnumRef() != null) {
            return entity.getAttribute().getEnumRef().getValues().stream()
                    .map(EnumValueEntry::getValue)
                    .toArray(String[]::new);
        }
        return null;
    }
}
