package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.CourseInviteDTO;
import org.trackdev.api.entity.CourseInvite;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface CourseInviteMapper {

    @Named("inviteToDTO")
    @Mapping(target = "invitedByName", source = "invitedBy.username")
    @Mapping(target = "acceptedByName", source = "acceptedBy.username")
    CourseInviteDTO toDTO(CourseInvite invite);

    @IterableMapping(qualifiedByName = "inviteToDTO")
    Collection<CourseInviteDTO> toDTOList(Collection<CourseInvite> invites);
}
