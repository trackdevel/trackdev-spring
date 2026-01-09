package org.trackdev.api.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Date;

import org.trackdev.api.configuration.DateFormattingConfiguration;

@Entity
@DiscriminatorValue(value = SprintEndDateChange.CHANGE_TYPE_NAME)
public class SprintEndDateChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "end_date_change";

    public SprintEndDateChange() {}

    public SprintEndDateChange(String author, Long sprint, Date date) {
        super(author, sprint);
        this.endDate = date;
    }

    private Date endDate;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonFormat(pattern = DateFormattingConfiguration.SIMPLE_LOCALDATE_FORMAT)
    public Date getEndDate() {
        return this.endDate;
    }
}
