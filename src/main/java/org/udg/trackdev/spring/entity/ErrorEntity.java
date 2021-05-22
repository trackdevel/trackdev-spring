package org.udg.trackdev.spring.entity;

import java.util.Objects;

public class ErrorEntity {

  public ErrorEntity(String timestamp, Integer status, String error, String message) {
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    this.status = Objects.requireNonNull(status, "status must not be null");
    this.error = Objects.requireNonNull(error, "error must not be null");
    this.message = Objects.requireNonNull(message, "message must not be null");
  }

  private String timestamp;
  private Integer status;
  private String error;
  private String message;

  public String getTimestamp() {
    return timestamp;
  }

  public Integer getStatus() {
    return status;
  }

  public String getError() {
    return error;
  }

  public String getMessage() {
    return message;
  }
}