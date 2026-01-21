package org.trackdev.api.entity.sprintchanges;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.ZonedDateTime;

import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.User;

@Entity
@DiscriminatorValue(value = SprintEndDateChange.CHANGE_TYPE_NAME)
public class SprintEndDateChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "end_date_change";

    public SprintEndDateChange() {}

    public SprintEndDateChange(User author, Sprint sprint, ZonedDateTime date) {
        super(author, sprint);
        this.endDate = date;
    }

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime endDate;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public ZonedDateTime getEndDate() {
        return this.endDate;
    }
}
