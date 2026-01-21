package org.trackdev.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "emails")
public class Email extends BaseEntityUUID{

    private String destination;

    @Column(columnDefinition = "TIMESTAMP")
    private ZonedDateTime timestamp;

    public Email() {
    }

    public Email(String destination, ZonedDateTime timestamp) {
        this.destination = destination;
        this.timestamp = timestamp;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }


}
