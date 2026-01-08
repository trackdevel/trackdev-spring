package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.PullRequestDTO;
import org.trackdev.api.entity.PullRequest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PullRequestMapper {

    @Named("pullRequestToDTO")
    @Mapping(target = "author", source = "author", qualifiedByName = "userToSummaryDTO")
    PullRequestDTO toDTO(PullRequest pullRequest);

    @IterableMapping(qualifiedByName = "pullRequestToDTO")
    List<PullRequestDTO> toDTOList(List<PullRequest> pullRequests);

    @IterableMapping(qualifiedByName = "pullRequestToDTO")
    Collection<PullRequestDTO> toDTOCollection(Collection<PullRequest> pullRequests);

    @IterableMapping(qualifiedByName = "pullRequestToDTO")
    Set<PullRequestDTO> toDTOSet(Set<PullRequest> pullRequests);
}
