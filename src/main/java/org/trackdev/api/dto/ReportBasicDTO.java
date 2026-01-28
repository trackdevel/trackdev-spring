package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.ReportAxisType;
import org.trackdev.api.entity.ReportElement;
import org.trackdev.api.entity.ReportMagnitude;

import java.time.ZonedDateTime;

@Data
public class ReportBasicDTO {
    private Long id;
    private String name;
    private ZonedDateTime createdAt;
    private UserSummaryDTO owner;
    private ReportAxisType rowType;
    private ReportAxisType columnType;
    private ReportElement element;
    private ReportMagnitude magnitude;
    private CourseBasicDTO course;
    /**
     * ID of the profile attribute used as custom magnitude source.
     * Null when using built-in magnitude (ESTIMATION_POINTS, PULL_REQUESTS).
     */
    private Long profileAttributeId;
    /**
     * Name of the profile attribute used as custom magnitude source.
     * Null when using built-in magnitude.
     */
    private String profileAttributeName;
}
