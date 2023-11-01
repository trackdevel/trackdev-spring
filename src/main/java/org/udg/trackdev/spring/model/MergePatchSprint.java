package org.udg.trackdev.spring.model;

import org.udg.trackdev.spring.entity.SprintStatus;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

/**
 *  The MergePatchTask models the request to edit a Sprint in a PATCH request
 *  following the JSON-based patch format JSON merge patch.
 */
public class MergePatchSprint {
    public Optional<String> name;

    public Optional<Date> startDate;

    public Optional<Date> endDate;

    public Optional<SprintStatus> status;
}