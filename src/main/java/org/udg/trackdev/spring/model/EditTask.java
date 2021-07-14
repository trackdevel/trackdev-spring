package org.udg.trackdev.spring.model;

import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.TaskStatus;

import javax.validation.constraints.Size;

public class EditTask {
    @Size(max = Task.NAME_LENGTH)
    public String name;

    public String assignee;

    public Integer estimationPoints;

    public TaskStatus status;
}