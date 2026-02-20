package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for ProfileEnum
 */
@Data
public class ProfileEnumDTO {
    private Long id;
    private String name;
    private List<EnumValueEntryDTO> values;
}
