package org.trackdev.api.controller.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested entity is not found.
 */
public class EntityNotFound extends BaseException {

    public EntityNotFound() {
        super("No such entity", HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }

    public EntityNotFound(String message) {
        super(message, HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }

    public EntityNotFound(String entityType, Object id) {
        super(entityType + " with id '" + id + "' not found", HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }
}