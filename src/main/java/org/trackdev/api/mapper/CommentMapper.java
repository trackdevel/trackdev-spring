package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.CommentDTO;
import org.trackdev.api.entity.Comment;

import java.util.Collection;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Named("commentToDTO")
    @Mapping(target = "author", source = "author", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "createdAt", source = "date")
    CommentDTO toDTO(Comment comment);

    @IterableMapping(qualifiedByName = "commentToDTO")
    Collection<CommentDTO> toDTOList(Collection<Comment> comments);
}
