package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.ReportAxisType;
import org.trackdev.api.entity.ReportElement;
import org.trackdev.api.entity.ReportMagnitude;

import java.util.Date;

@Data
public class ReportBasicDTO {
    private Long id;
    private String name;
    private Date createdAt;
    private UserSummaryDTO owner;
    private ReportAxisType rowType;
    private ReportAxisType columnType;
    private ReportElement element;
    private ReportMagnitude magnitude;
    private CourseBasicDTO course;
}
