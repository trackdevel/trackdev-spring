package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning a Long ID
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdResponseDTO {
    private Long id;
}
