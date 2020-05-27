package org.udg.trackdev.spring.controller.exceptions;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;
import org.udg.trackdev.spring.service.Global;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.ErrorEntity;

import java.util.Date;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    public RestResponseEntityExceptionHandler() {
        super();
    }

    // Other exceptions. Add your own exception handling here

    @ExceptionHandler(value={ Exception.class })
    protected ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request) {

        if (ex instanceof ServiceException) {
            return handleExceptionInternal(ex,
                    new ErrorEntity(Global.dateFormat.format(new Date()),
                            HttpStatus.BAD_REQUEST.value(),
                            "Service error",
                            ex.getMessage()),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        } else if (ex instanceof ControllerException) {
            return handleExceptionInternal(ex,
                    new ErrorEntity(Global.dateFormat.format(new Date()),
                            HttpStatus.BAD_REQUEST.value(),
                            "Controller error",
                            ex.getMessage()),
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        } else if (ex instanceof java.lang.SecurityException) {
            return handleExceptionInternal(ex,
                    new ErrorEntity(Global.dateFormat.format(new Date()),
                            HttpStatus.FORBIDDEN.value(),
                            "Security error",
                            ex.getMessage()),
                    new HttpHeaders(), HttpStatus.FORBIDDEN, request);
        } else
            return handleExceptionInternal(ex,
                    new ErrorEntity(Global.dateFormat.format(new Date()),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Unknown error",
                            ex.getMessage()),
                    new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        if (body == null) {
            body = new ErrorEntity(Global.dateFormat.format(new Date()), status.value(), "Unknown error", ex.getMessage());
        }
        return new ResponseEntity<>(body, headers, status);
    }

}
