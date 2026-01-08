package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when access is denied due to security constraints.
 */
public class SecurityException extends BaseException {

    public SecurityException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    public SecurityException(String message, String errorCode) {
        super(message, HttpStatus.FORBIDDEN, errorCode);
    }
}