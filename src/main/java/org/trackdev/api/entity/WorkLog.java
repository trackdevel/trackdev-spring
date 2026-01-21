package org.trackdev.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "work_logs")
public class WorkLog extends BaseEntityLong {

    @ManyToOne
    private User author;

    @ManyToOne
    private Task task;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime timeStamp;

    private Integer timeSeconds;
}
