package org.trackdev.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Collection;

public class CreatePointsReviewRequest {

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content;

    @NotNull(message = "Proposed points are required")
    @Min(value = 0, message = "Proposed points must be at least 0")
    private Integer proposedPoints;

    private Collection<Long> similarTaskIds;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getProposedPoints() { return proposedPoints; }
    public void setProposedPoints(Integer proposedPoints) { this.proposedPoints = proposedPoints; }

    public Collection<Long> getSimilarTaskIds() { return similarTaskIds; }
    public void setSimilarTaskIds(Collection<Long> similarTaskIds) { this.similarTaskIds = similarTaskIds; }
}
