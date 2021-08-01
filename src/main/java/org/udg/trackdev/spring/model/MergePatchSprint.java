package org.udg.trackdev.spring.model;

import java.util.Calendar;
import java.util.Optional;

/**
 *  The MergePatchTask models the request to edit a Sprint in a PATCH request
 *  following the JSON-based patch format JSON merge patch.
 */
public class MergePatchSprint {
    public Optional<String> name;

    public Optional<Calendar> startDate;

    public Optional<Calendar> endDate;
}