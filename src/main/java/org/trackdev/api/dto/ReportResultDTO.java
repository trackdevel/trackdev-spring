package org.trackdev.api.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO representing the computed result of a report for a project.
 * Contains a grid of values with row and column headers.
 */
public class ReportResultDTO {

    private Long reportId;
    private String reportName;
    private Long projectId;
    private String projectName;
    
    // Axis types
    private String rowType;    // "STUDENTS" or "SPRINTS"
    private String columnType; // "STUDENTS" or "SPRINTS"
    private String element;    // "TASK"
    private String magnitude;  // "ESTIMATION_POINTS", "PULL_REQUESTS", or "PROFILE_ATTRIBUTE"
    
    // Profile attribute info (when magnitude is PROFILE_ATTRIBUTE)
    private Long profileAttributeId;
    private String profileAttributeName;
    
    // Headers for rows and columns
    private List<AxisHeader> rowHeaders;    // List of {id, name}
    private List<AxisHeader> columnHeaders; // List of {id, name}
    
    // Grid data: map of "rowId:columnId" -> value
    private Map<String, Integer> data;
    
    // Row totals: map of rowId -> total
    private Map<String, Integer> rowTotals;
    
    // Column totals: map of columnId -> total
    private Map<String, Integer> columnTotals;
    
    // Grand total
    private Integer grandTotal;

    // Constructors
    public ReportResultDTO() {}

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRowType() {
        return rowType;
    }

    public void setRowType(String rowType) {
        this.rowType = rowType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(String magnitude) {
        this.magnitude = magnitude;
    }

    public Long getProfileAttributeId() {
        return profileAttributeId;
    }

    public void setProfileAttributeId(Long profileAttributeId) {
        this.profileAttributeId = profileAttributeId;
    }

    public String getProfileAttributeName() {
        return profileAttributeName;
    }

    public void setProfileAttributeName(String profileAttributeName) {
        this.profileAttributeName = profileAttributeName;
    }

    public List<AxisHeader> getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(List<AxisHeader> rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    public List<AxisHeader> getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(List<AxisHeader> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public Map<String, Integer> getData() {
        return data;
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
    }

    public Map<String, Integer> getRowTotals() {
        return rowTotals;
    }

    public void setRowTotals(Map<String, Integer> rowTotals) {
        this.rowTotals = rowTotals;
    }

    public Map<String, Integer> getColumnTotals() {
        return columnTotals;
    }

    public void setColumnTotals(Map<String, Integer> columnTotals) {
        this.columnTotals = columnTotals;
    }

    public Integer getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Integer grandTotal) {
        this.grandTotal = grandTotal;
    }

    /**
     * Inner class for axis headers (row or column).
     */
    public static class AxisHeader {
        private String id;
        private String name;

        public AxisHeader() {}

        public AxisHeader(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
