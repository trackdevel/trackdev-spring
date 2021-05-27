package org.udg.trackdev.spring.entity;

import lombok.Data;
import lombok.NonNull;

@Data
public class ErrorEntity {
  @NonNull private String timestamp;
  @NonNull private Integer status;
  @NonNull private String error;
  @NonNull private String message;
}