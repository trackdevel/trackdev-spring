package org.udg.trackdev.spring.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs")
public class WorkLog extends BaseEntityLong {

    @ManyToOne
    private User author;

    @ManyToOne
    private Task task;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime timeStamp;

    private Integer timeSeconds;
}
