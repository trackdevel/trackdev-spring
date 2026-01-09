package org.trackdev.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.trackdev.api.dto.GithubInfoDTO;
import org.trackdev.api.dto.GithubInfoWithTokenDTO;
import org.trackdev.api.entity.GithubInfo;

@Mapper(componentModel = "spring")
public interface GithubInfoMapper {

    @Named("githubInfoToDTO")
    GithubInfoDTO toDTO(GithubInfo githubInfo);

    @Named("githubInfoToWithTokenDTO")
    GithubInfoWithTokenDTO toWithTokenDTO(GithubInfo githubInfo);
}
