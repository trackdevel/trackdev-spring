package org.udg.trackdev.spring.entity;

import javax.persistence.*;
import java.io.Serializable;
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
