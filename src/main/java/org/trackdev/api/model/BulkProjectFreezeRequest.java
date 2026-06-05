package org.trackdev.api.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkProjectFreezeRequest {

    @NotEmpty
    public List<Long> projectIds;

    @NotNull
    public Boolean frozen;
}
