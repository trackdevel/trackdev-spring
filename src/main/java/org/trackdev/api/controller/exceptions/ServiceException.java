package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service-level business rule is violated.
 * Supports capturing cause exception details for enhanced error reporting.
 */
public class ServiceException extends BaseException {

    public ServiceException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "SERVICE_ERROR");
    }

    public ServiceException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    /**
     * Creates a ServiceException with the original cause exception.
     * The cause details will be automatically captured.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "SERVICE_ERROR");
    }

    /**
     * Creates a ServiceException with the original cause exception and custom error code.
     * The cause details will be automatically captured.
     */
    public ServiceException(String message, Throwable cause, String errorCode) {
        super(message, cause, HttpStatus.BAD_REQUEST, errorCode);
    }
}