package org.trackdev.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.SprintPatternDTO;
import org.trackdev.api.dto.SprintPatternItemDTO;
import org.trackdev.api.entity.SprintPattern;
import org.trackdev.api.entity.SprintPatternItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SprintPatternMapper {

    @Named("toItemDTO")
    SprintPatternItemDTO toItemDTO(SprintPatternItem item);

    List<SprintPatternItemDTO> toItemDTOList(List<SprintPatternItem> items);

    @Named("toDTO")
    @Mapping(target = "items", source = "items")
    SprintPatternDTO toDTO(SprintPattern pattern);

    List<SprintPatternDTO> toDTOList(List<SprintPattern> patterns);
}
