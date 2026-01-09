package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error related to entity operations.
 */
public class EntityException extends BaseException {

    public EntityException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "ENTITY_ERROR");
    }

    public EntityException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}