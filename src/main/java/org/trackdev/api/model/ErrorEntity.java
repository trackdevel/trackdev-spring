package org.trackdev.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorEntity {
  @NonNull private String timestamp;
  @NonNull private Integer status;
  @NonNull private String error;
  @NonNull private String message;
  private String code;
  private String path;

  public ErrorEntity(@NonNull String timestamp, @NonNull Integer status, 
                     @NonNull String error, @NonNull String message) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
  }

  public ErrorEntity(@NonNull String timestamp, @NonNull Integer status,
                     @NonNull String error, @NonNull String message,
                     String code, String path) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.code = code;
    this.path = path;
  }
}