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
import java.util.Date;

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

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_LOCALDATE_FORMAT)
    public Date getEndDate() {
        return this.endDate;
    }
}
