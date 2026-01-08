package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning a UUID (String) ID
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdUUIDResponseDTO {
    private String id;
}
