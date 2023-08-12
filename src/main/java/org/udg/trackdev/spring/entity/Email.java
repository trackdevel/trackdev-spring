package org.udg.trackdev.spring.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
public class Email extends BaseEntityUUID{

    private String destination;

    private LocalDateTime timestamp;

    public Email() {
    }

    public Email(String destination, LocalDateTime timestamp) {
        this.destination = destination;
        this.timestamp = timestamp;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


}
