package org.udg.trackdev.spring.model;

import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.TaskStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

/**
 *  The MergePatchTask models the request to edit a Task in a PATCH request
 *  following the JSON-based patch format JSON merge patch.
 *
 *  In JSON merge patch, only present fields should be changed or added.
 *  Fields present with a "null" value should be deleted.
 *  Non present fields should be not touched and maintain their current value.
 *
 *  This class uses the Optional class from Java util to detect the presence
 *  of the field.
 */
public class MergePatchTask {
    public Optional<String> name;

    public Optional<String> assignee;

    public Optional<Integer> estimationPoints;

    public Optional<TaskStatus> status;

    public Optional<Integer> rank;
}