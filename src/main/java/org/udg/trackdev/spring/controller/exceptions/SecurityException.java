package org.udg.trackdev.spring.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN)
public class SecurityException extends RuntimeException {
    public SecurityException(String message) {
        super(message);
    }
}