package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.EnumValueEntryDTO;
import org.trackdev.api.dto.ListItemDTO;
import org.trackdev.api.dto.PullRequestAttributeListValueDTO;
import org.trackdev.api.dto.PullRequestAttributeValueDTO;
import org.trackdev.api.entity.AttributeType;
import org.trackdev.api.entity.EnumValueEntry;
import org.trackdev.api.entity.ProfileAttribute;
import org.trackdev.api.entity.PullRequestAttributeListValue;
import org.trackdev.api.entity.PullRequestAttributeValue;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PullRequestAttributeValueMapper {

    @Named("toDTO")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    @Mapping(target = "attributeAppliedBy", source = "attribute.appliedBy")
    @Mapping(target = "value", expression = "java(getEffectiveValue(entity))")
    @Mapping(target = "textValue", expression = "java(getEffectiveTextValue(entity))")
    @Mapping(target = "enumValues", expression = "java(getEnumValues(entity))")
    PullRequestAttributeValueDTO toDTO(PullRequestAttributeValue entity);

    @IterableMapping(qualifiedByName = "toDTO")
    List<PullRequestAttributeValueDTO> toDTOList(List<PullRequestAttributeValue> entities);

    default String getEffectiveValue(PullRequestAttributeValue entity) {
        if (entity.getAttribute() != null && entity.getAttribute().getType() == AttributeType.TEXT) {
            return entity.getTextValue();
        }
        return entity.getValue();
    }

    default String getEffectiveTextValue(PullRequestAttributeValue entity) {
        if (entity.getAttribute() != null && entity.getAttribute().getType() == AttributeType.NUMERIC_TEXT) {
            return entity.getTextValue();
        }
        return null;
    }

    default EnumValueEntryDTO[] getEnumValues(PullRequestAttributeValue entity) {
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

    default ListItemDTO toListItemDTO(PullRequestAttributeListValue entity) {
        ListItemDTO dto = new ListItemDTO();
        dto.setOrderIndex(entity.getOrderIndex());
        dto.setEnumValue(entity.getEnumValue());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    default PullRequestAttributeListValueDTO toListValueDTO(ProfileAttribute attribute, List<PullRequestAttributeListValue> items) {
        PullRequestAttributeListValueDTO dto = new PullRequestAttributeListValueDTO();
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
