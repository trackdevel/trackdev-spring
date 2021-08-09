package org.udg.trackdev.spring.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.SprintStatus;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = SprintStatusChange.CHANGE_TYPE_NAME)
public class SprintStatusChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "status_change";

    public SprintStatusChange() { }

    public SprintStatusChange(User author, Sprint sprint, SprintStatus status) {
        super(author, sprint);
        this.status = status;
    }

    @Column(name = "`status`")
    private SprintStatus status;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public SprintStatus getStatus() {
        return this.status;
    }
}
