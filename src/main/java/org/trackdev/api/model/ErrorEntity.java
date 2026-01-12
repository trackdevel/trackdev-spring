package org.trackdev.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorEntity {
  @NonNull private String timestamp;
  @NonNull private Integer status;
  @NonNull private String error;
  @NonNull private String message;
  private String code;
  private String path;
  private List<FieldError> fieldErrors;
  private Map<String, Object> details;

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

  /**
   * Represents a single field validation error with detailed information.
   */
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FieldError {
    private String field;
    private Object rejectedValue;
    private String message;
    private String code;

    public FieldError(String field, Object rejectedValue, String message, String code) {
      this.field = field;
      this.rejectedValue = rejectedValue;
      this.message = message;
      this.code = code;
    }
  }
}