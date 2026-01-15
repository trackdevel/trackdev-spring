package org.trackdev.api.model;

import jakarta.validation.constraints.Size;
import org.trackdev.api.entity.Workspace;

import java.util.Optional;

public class MergePatchWorkspace {

    @Size(min = Workspace.MIN_NAME_LENGTH, max = Workspace.NAME_LENGTH, 
          message = "Workspace name must be between 1 and 100 characters")
    public Optional<String> name;

    public MergePatchWorkspace() {}
}
