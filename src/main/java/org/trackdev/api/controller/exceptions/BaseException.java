package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all application exceptions.
 * Provides consistent error handling with HTTP status and error codes.
 */
public abstract class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private Map<String, Object> details;

    protected BaseException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    protected BaseException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, null);
    }

    protected BaseException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        // Automatically capture cause details
        if (cause != null) {
            this.details = new HashMap<>();
            this.details.put("causeType", cause.getClass().getSimpleName());
            if (cause.getMessage() != null) {
                this.details.put("causeMessage", cause.getMessage());
            }
        }
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public BaseException addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
}
