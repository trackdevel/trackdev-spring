package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error in the controller layer (validation, bad request, etc.).
 */
public class ControllerException extends BaseException {

    public ControllerException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "CONTROLLER_ERROR");
    }

    public ControllerException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}