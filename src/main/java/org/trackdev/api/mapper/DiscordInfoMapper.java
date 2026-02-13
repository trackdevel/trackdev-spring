package org.trackdev.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.trackdev.api.dto.DiscordInfoDTO;
import org.trackdev.api.entity.DiscordInfo;

@Mapper(componentModel = "spring")
public interface DiscordInfoMapper {

    @Named("discordInfoToDTO")
    DiscordInfoDTO toDTO(DiscordInfo discordInfo);
}
