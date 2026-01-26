package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.ProfileAttributeDTO;
import org.trackdev.api.dto.ProfileBasicDTO;
import org.trackdev.api.dto.ProfileCompleteDTO;
import org.trackdev.api.dto.ProfileEnumDTO;
import org.trackdev.api.entity.Profile;
import org.trackdev.api.entity.ProfileAttribute;
import org.trackdev.api.entity.ProfileEnum;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Named("profileToBasicDTO")
    ProfileBasicDTO toBasicDTO(Profile profile);

    @Named("profileToCompleteDTO")
    @Mapping(target = "enums", source = "enums", qualifiedByName = "enumsToDTO")
    @Mapping(target = "attributes", source = "attributes", qualifiedByName = "attributesToDTO")
    ProfileCompleteDTO toCompleteDTO(Profile profile);

    @IterableMapping(qualifiedByName = "profileToBasicDTO")
    List<ProfileBasicDTO> toBasicDTOList(List<Profile> profiles);

    @Named("enumToDTO")
    ProfileEnumDTO enumToDTO(ProfileEnum profileEnum);

    @Named("enumsToDTO")
    @IterableMapping(qualifiedByName = "enumToDTO")
    List<ProfileEnumDTO> enumsToDTO(List<ProfileEnum> enums);

    @Named("attributeToDTO")
    @Mapping(target = "enumRefName", source = "enumRef.name")
    ProfileAttributeDTO attributeToDTO(ProfileAttribute attribute);

    @Named("attributesToDTO")
    @IterableMapping(qualifiedByName = "attributeToDTO")
    List<ProfileAttributeDTO> attributesToDTO(List<ProfileAttribute> attributes);
}
