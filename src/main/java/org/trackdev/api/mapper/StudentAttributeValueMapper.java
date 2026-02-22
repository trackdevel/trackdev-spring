package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.EnumValueEntryDTO;
import org.trackdev.api.dto.ListItemDTO;
import org.trackdev.api.dto.StudentAttributeListValueDTO;
import org.trackdev.api.dto.StudentAttributeValueDTO;
import org.trackdev.api.entity.EnumValueEntry;
import org.trackdev.api.entity.ProfileAttribute;
import org.trackdev.api.entity.StudentAttributeListValue;
import org.trackdev.api.entity.StudentAttributeValue;

import java.util.List;
import java.util.stream.Collectors;

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

    default EnumValueEntryDTO[] getEnumValues(StudentAttributeValue entity) {
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

    // ==================== LIST value mapping ====================

    default ListItemDTO toListItemDTO(StudentAttributeListValue entity) {
        ListItemDTO dto = new ListItemDTO();
        dto.setOrderIndex(entity.getOrderIndex());
        dto.setEnumValue(entity.getEnumValue());
        dto.setStringValue(entity.getStringValue());
        return dto;
    }

    default StudentAttributeListValueDTO toListValueDTO(ProfileAttribute attribute, List<StudentAttributeListValue> items) {
        StudentAttributeListValueDTO dto = new StudentAttributeListValueDTO();
        dto.setAttributeId(attribute.getId());
        dto.setAttributeName(attribute.getName());
        dto.setAttributeType("LIST");
        dto.setItems(items.stream().map(this::toListItemDTO).collect(Collectors.toList()));
        if (attribute.getEnumRef() != null) {
            dto.setEnumValues(attribute.getEnumRef().getValues().stream()
                    .map(this::toEnumValueEntryDTO)
                    .toArray(EnumValueEntryDTO[]::new));
        }
        return dto;
    }
}
