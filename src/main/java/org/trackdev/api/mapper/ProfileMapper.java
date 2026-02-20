package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.EnumValueEntryDTO;
import org.trackdev.api.dto.ProfileAttributeDTO;
import org.trackdev.api.dto.ProfileBasicDTO;
import org.trackdev.api.dto.ProfileCompleteDTO;
import org.trackdev.api.dto.ProfileEnumDTO;
import org.trackdev.api.entity.EnumValueEntry;
import org.trackdev.api.entity.Profile;
import org.trackdev.api.entity.ProfileAttribute;
import org.trackdev.api.entity.ProfileEnum;

import java.util.Collections;
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
    @Mapping(target = "values", source = "values", qualifiedByName = "enumValuesToDTO")
    ProfileEnumDTO enumToDTO(ProfileEnum profileEnum);

    @Named("enumsToDTO")
    @IterableMapping(qualifiedByName = "enumToDTO")
    List<ProfileEnumDTO> enumsToDTO(List<ProfileEnum> enums);

    @Named("enumValueEntryToDTO")
    EnumValueEntryDTO enumValueEntryToDTO(EnumValueEntry entry);

    @Named("enumValuesToDTO")
    @IterableMapping(qualifiedByName = "enumValueEntryToDTO")
    List<EnumValueEntryDTO> enumValuesToDTO(List<EnumValueEntry> entries);

    @Named("attributeToDTO")
    @Mapping(target = "enumRefName", source = "enumRef.name")
    @Mapping(target = "enumValues", source = "attribute", qualifiedByName = "getEnumValuesFromAttribute")
    ProfileAttributeDTO attributeToDTO(ProfileAttribute attribute);

    @Named("attributesToDTO")
    @IterableMapping(qualifiedByName = "attributeToDTO")
    List<ProfileAttributeDTO> attributesToDTO(List<ProfileAttribute> attributes);

    @Named("getEnumValuesFromAttribute")
    default List<EnumValueEntryDTO> getEnumValuesFromAttribute(ProfileAttribute attribute) {
        if (attribute.getEnumRef() != null) {
            return attribute.getEnumRef().getValues().stream()
                    .map(this::enumValueEntryToDTO)
                    .toList();
        }
        return Collections.emptyList();
    }
}
