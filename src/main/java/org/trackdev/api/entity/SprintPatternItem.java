package org.trackdev.api.entity;

import org.springframework.lang.NonNull;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * A single sprint blueprint within a sprint pattern.
 * Defines the name and dates for a sprint that will be created when the pattern is applied.
 */
@Entity
@Table(name = "sprint_pattern_items")
public class SprintPatternItem extends BaseEntityLong {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 50;

    @NonNull
    private String name;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime startDate;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime endDate;

    /**
     * The order of this sprint item within the pattern (0-indexed)
     */
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    private SprintPattern sprintPattern;

    @Column(name = "sprint_pattern_id", insertable = false, updatable = false)
    private Long sprintPatternId;

    public SprintPatternItem() {}

    public SprintPatternItem(String name, ZonedDateTime startDate, ZonedDateTime endDate, Integer orderIndex) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderIndex = orderIndex;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public SprintPattern getSprintPattern() {
        return sprintPattern;
    }

    public void setSprintPattern(SprintPattern sprintPattern) {
        this.sprintPattern = sprintPattern;
    }

    public Long getSprintPatternId() {
        return sprintPatternId;
    }
}
