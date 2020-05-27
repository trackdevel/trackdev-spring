package org.udg.trackdev.spring.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "task_logs")
public class TaskLog extends BaseEntityLong {

    @ManyToOne
    private Task task;

    @ManyToOne
    private Sprint sprint;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime timeStamp;

    private TaskStatus taskStatus;
}
