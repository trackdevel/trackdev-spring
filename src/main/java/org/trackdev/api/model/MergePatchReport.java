package org.trackdev.api.model;

import org.trackdev.api.entity.ReportAxisType;
import org.trackdev.api.entity.ReportElement;
import org.trackdev.api.entity.ReportMagnitude;

import java.util.Optional;

/**
 * The MergePatchReport models the request to edit a Report in a PATCH request
 * following the JSON-based patch format JSON merge patch.
 *
 * In JSON merge patch, only present fields should be changed or added.
 * Fields present with a "null" value should be deleted.
 * Non present fields should be not touched and maintain their current value.
 *
 * This class uses the Optional class from Java util to detect the presence
 * of the field.
 */
public class MergePatchReport {
    public Optional<String> name;
    public Optional<ReportAxisType> rowType;
    public Optional<ReportAxisType> columnType;
    public Optional<ReportElement> element;
    public Optional<ReportMagnitude> magnitude;
    public Optional<Long> courseId;
    /**
     * Optional reference to a profile attribute for custom magnitude.
     * When set, the report uses this attribute's values instead of built-in magnitudes.
     * Set to null to clear the profile attribute (use built-in magnitude instead).
     */
    public Optional<Long> profileAttributeId;
}
