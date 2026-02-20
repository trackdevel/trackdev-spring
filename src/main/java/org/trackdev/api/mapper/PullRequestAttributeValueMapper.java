package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.PullRequestAttributeValueDTO;
import org.trackdev.api.entity.EnumValueEntry;
import org.trackdev.api.entity.PullRequestAttributeValue;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PullRequestAttributeValueMapper {

    @Named("toDTO")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    @Mapping(target = "attributeAppliedBy", source = "attribute.appliedBy")
    @Mapping(target = "enumValues", expression = "java(getEnumValues(entity))")
    PullRequestAttributeValueDTO toDTO(PullRequestAttributeValue entity);

    @IterableMapping(qualifiedByName = "toDTO")
    List<PullRequestAttributeValueDTO> toDTOList(List<PullRequestAttributeValue> entities);

    default String[] getEnumValues(PullRequestAttributeValue entity) {
        if (entity.getAttribute() != null &&
            entity.getAttribute().getEnumRef() != null) {
            return entity.getAttribute().getEnumRef().getValues().stream()
                    .map(EnumValueEntry::getValue)
                    .toArray(String[]::new);
        }
        return null;
    }
}
