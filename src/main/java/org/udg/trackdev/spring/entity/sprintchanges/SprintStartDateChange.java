package org.udg.trackdev.spring.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.Global;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue(value = SprintStartDateChange.CHANGE_TYPE_NAME)
public class SprintStartDateChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "start_date_change";

    public SprintStartDateChange() {}

    public SprintStartDateChange(User author, Sprint sprint, LocalDate value) {
        super(author, sprint);
        this.startDate = value;
    }

    private LocalDate startDate;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public LocalDate getStartDate() {
        return this.startDate;
    }
}
