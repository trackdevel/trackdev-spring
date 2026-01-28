package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.ReportBasicDTO;
import org.trackdev.api.entity.Report;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CourseMapper.class})
public interface ReportMapper {

    @Named("reportToBasicDTO")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "course", source = "course", qualifiedByName = "courseToBasicDTO")
    @Mapping(target = "profileAttributeId", source = "profileAttribute.id")
    @Mapping(target = "profileAttributeName", source = "profileAttribute.name")
    ReportBasicDTO toBasicDTO(Report report);

    @IterableMapping(qualifiedByName = "reportToBasicDTO")
    List<ReportBasicDTO> toBasicDTOList(List<Report> reports);
}
