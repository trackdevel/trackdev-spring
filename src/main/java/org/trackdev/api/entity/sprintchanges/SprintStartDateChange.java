package org.trackdev.api.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Date;

import org.trackdev.api.service.Global;

@Entity
@DiscriminatorValue(value = SprintStartDateChange.CHANGE_TYPE_NAME)
public class SprintStartDateChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "start_date_change";

    public SprintStartDateChange() {}

    public SprintStartDateChange(String author, Long sprint, Date value) {
        super(author, sprint);
        this.startDate = value;
    }

    private Date startDate;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public Date getStartDate() {
        return this.startDate;
    }
}
