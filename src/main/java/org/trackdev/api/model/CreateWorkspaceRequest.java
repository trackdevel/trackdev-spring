package org.trackdev.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.trackdev.api.entity.Workspace;

public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(min = Workspace.MIN_NAME_LENGTH, max = Workspace.NAME_LENGTH, 
          message = "Workspace name must be between 1 and 100 characters")
    private String name;

    public CreateWorkspaceRequest() {}

    public CreateWorkspaceRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
