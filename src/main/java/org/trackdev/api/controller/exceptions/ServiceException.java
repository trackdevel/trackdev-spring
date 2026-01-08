package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service-level business rule is violated.
 */
public class ServiceException extends BaseException {

    public ServiceException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "SERVICE_ERROR");
    }

    public ServiceException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}