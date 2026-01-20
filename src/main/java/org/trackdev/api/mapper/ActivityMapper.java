package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.ActivityDTO;
import org.trackdev.api.entity.Activity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Named("activityToDTO")
    @Mapping(target = "type", expression = "java(activity.getType().name())")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "actorUsername", source = "actorUsername")
    @Mapping(target = "actorEmail", source = "actorEmail")
    @Mapping(target = "projectId", source = "projectId")
    @Mapping(target = "projectName", source = "projectName")
    @Mapping(target = "taskId", source = "taskId")
    @Mapping(target = "taskKey", source = "taskKey")
    @Mapping(target = "taskName", source = "taskName")
    ActivityDTO toDTO(Activity activity);

    @IterableMapping(qualifiedByName = "activityToDTO")
    List<ActivityDTO> toDTOList(List<Activity> activities);
}
