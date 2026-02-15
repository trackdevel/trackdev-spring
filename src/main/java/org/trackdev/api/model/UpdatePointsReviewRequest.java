package org.trackdev.api.model;

import jakarta.validation.constraints.Min;

import java.util.Collection;

public class UpdatePointsReviewRequest {

    @Min(value = 0, message = "Proposed points must be at least 0")
    private Integer proposedPoints;

    private Collection<Long> similarTaskIds;

    public Integer getProposedPoints() { return proposedPoints; }
    public void setProposedPoints(Integer proposedPoints) { this.proposedPoints = proposedPoints; }

    public Collection<Long> getSimilarTaskIds() { return similarTaskIds; }
    public void setSimilarTaskIds(Collection<Long> similarTaskIds) { this.similarTaskIds = similarTaskIds; }
}
