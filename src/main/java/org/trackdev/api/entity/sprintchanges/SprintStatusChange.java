package org.trackdev.api.entity.sprintchanges;

import org.trackdev.api.entity.SprintStatus;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = SprintStatusChange.CHANGE_TYPE_NAME)
public class SprintStatusChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "status_change";

    public SprintStatusChange() { }

    public SprintStatusChange(String author, Long sprint, SprintStatus status) {
        super(author, sprint);
        this.status = status;
    }

    @Column(name = "`status`")
    private SprintStatus status;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    public SprintStatus getStatus() {
        return this.status;
    }
}
