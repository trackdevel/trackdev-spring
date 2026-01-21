package org.trackdev.api.entity.sprintchanges;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.ZonedDateTime;

import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.User;

@Entity
@DiscriminatorValue(value = SprintStartDateChange.CHANGE_TYPE_NAME)
public class SprintStartDateChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "start_date_change";

    public SprintStartDateChange() {}

    public SprintStartDateChange(User author, Sprint sprint, ZonedDateTime value) {
        super(author, sprint);
        this.startDate = value;
    }

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime startDate;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public ZonedDateTime getStartDate() {
        return this.startDate;
    }
}
