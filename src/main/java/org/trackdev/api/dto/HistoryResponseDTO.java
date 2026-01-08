package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for history response (generic for sprint/task changes)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponseDTO<T> {
    private List<T> history;
    private Long entityId;
}
