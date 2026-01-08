package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.trackdev.api.serializer.JsonDateSerializer;
import org.trackdev.api.service.Global;

import jakarta.persistence.*;
import java.util.Date;

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

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date startDate;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date endDate;

    /**
     * The order of this sprint item within the pattern (0-indexed)
     */
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprintPatternId")
    private SprintPattern sprintPattern;

    @Column(name = "sprintPatternId", insertable = false, updatable = false)
    private Long sprintPatternId;

    public SprintPatternItem() {}

    public SprintPatternItem(String name, Date startDate, Date endDate, Integer orderIndex) {
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

    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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
