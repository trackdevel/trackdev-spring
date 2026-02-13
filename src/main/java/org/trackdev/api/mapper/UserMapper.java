package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.Role;
import org.trackdev.api.entity.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { GithubInfoMapper.class, DiscordInfoMapper.class })
public interface UserMapper {

    @Named("userToBasicDTO")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "githubInfo", source = "githubInfo", qualifiedByName = "githubInfoToDTO")
    UserBasicDTO toBasicDTO(User user);

    @Named("userToSummaryDTO")
    @Mapping(target = "githubInfo", source = "githubInfo", qualifiedByName = "githubInfoToDTO")
    UserSummaryDTO toSummaryDTO(User user);

    @Named("userToWithProjectsDTO")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "githubInfo", source = "githubInfo", qualifiedByName = "githubInfoToDTO")
    @Mapping(target = "discordInfo", source = "discordInfo", qualifiedByName = "discordInfoToDTO")
    @Mapping(target = "projects", ignore = true)
    UserWithProjectsDTO toWithProjectsDTO(User user);

    @Named("userToWithGithubTokenDTO")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "githubInfo", source = "githubInfo", qualifiedByName = "githubInfoToWithTokenDTO")
    @Mapping(target = "discordInfo", source = "discordInfo", qualifiedByName = "discordInfoToDTO")
    @Mapping(target = "projects", ignore = true)
    UserWithGithubTokenDTO toWithGithubTokenDTO(User user);

    @IterableMapping(qualifiedByName = "userToWithProjectsDTO")
    Collection<UserWithProjectsDTO> toWithProjectsDTOList(Collection<User> users);

    @IterableMapping(qualifiedByName = "userToSummaryDTO")
    Collection<UserSummaryDTO> toSummaryDTOList(Collection<User> users);

    @IterableMapping(qualifiedByName = "userToSummaryDTO")
    Set<UserSummaryDTO> toSummaryDTOSet(Set<User> users);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getUserType().name())
                .collect(Collectors.toSet());
    }
}
