package org.trackdev.api.controller.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.trackdev.api.configuration.DateFormattingConfiguration;
import org.trackdev.api.model.ErrorEntity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Global exception handler that intercepts ALL exceptions and returns
 * a consistent ErrorEntity response structure.
 * 
 * Extends ResponseEntityExceptionHandler to properly intercept Spring MVC exceptions
 * before they get converted to Problem Details format.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    // ========== Override all Spring MVC exception handlers ==========

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Method '" + ex.getMethod() + "' is not supported";
        if (ex.getSupportedMethods() != null) {
            message += ". Supported: " + String.join(", ", ex.getSupportedMethods());
        }
        log.warn("Method not supported: {}", message);
        return createErrorResponse("Method not allowed", HttpStatus.METHOD_NOT_ALLOWED, message, "METHOD_NOT_ALLOWED", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Media type '" + ex.getContentType() + "' is not supported";
        log.warn("Media type not supported: {}", message);
        return createErrorResponse("Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, "UNSUPPORTED_MEDIA_TYPE", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Media type not acceptable");
        return createErrorResponse("Not acceptable", HttpStatus.NOT_ACCEPTABLE, 
                "Could not find acceptable representation", "NOT_ACCEPTABLE", request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Missing path variable: " + ex.getVariableName();
        log.warn(message);
        return createErrorResponse("Missing path variable", HttpStatus.BAD_REQUEST, message, "MISSING_PATH_VARIABLE", request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        log.warn(message);
        return createErrorResponse("Missing parameter", HttpStatus.BAD_REQUEST, message, "MISSING_PARAMETER", request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Required request part '" + ex.getRequestPartName() + "' is missing";
        log.warn(message);
        return createErrorResponse("Missing request part", HttpStatus.BAD_REQUEST, message, "MISSING_REQUEST_PART", request);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Request binding error: {}", ex.getMessage());
        return createErrorResponse("Request binding error", HttpStatus.BAD_REQUEST, ex.getMessage(), "BINDING_ERROR", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return createErrorResponse("Validation error", HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR", request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Handler method validation failed: {}", ex.getMessage());
        return createErrorResponse("Validation error", HttpStatus.BAD_REQUEST, 
                "Request validation failed", "VALIDATION_ERROR", request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        log.warn(message);
        return createErrorResponse("Not found", HttpStatus.NOT_FOUND, message, "ENDPOINT_NOT_FOUND", request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Resource not found: " + ex.getResourcePath();
        log.warn(message);
        return createErrorResponse("Not found", HttpStatus.NOT_FOUND, message, "RESOURCE_NOT_FOUND", request);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Async request timeout");
        return createErrorResponse("Request timeout", HttpStatus.SERVICE_UNAVAILABLE, 
                "The request timed out", "REQUEST_TIMEOUT", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Request body is missing or malformed";
        
        // Try to get more specific error message
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            message = "Invalid value for field: " + ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));
        } else if (cause instanceof JsonMappingException jme) {
            message = "JSON parsing error: " + jme.getOriginalMessage();
        }
        
        log.warn("Message not readable: {}", ex.getMessage());
        return createErrorResponse("Malformed request", HttpStatus.BAD_REQUEST, message, "MALFORMED_REQUEST", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.error("Message not writable: {}", ex.getMessage());
        return createErrorResponse("Internal error", HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error writing response", "WRITE_ERROR", request);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Max upload size exceeded");
        return createErrorResponse("File too large", HttpStatus.PAYLOAD_TOO_LARGE, 
                "The uploaded file exceeds the maximum allowed size", "FILE_TOO_LARGE", request);
    }

    /**
     * This is the key method - it's called by all other handlers in the parent class.
     * By overriding it, we ensure ALL responses go through our format.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        
        log.debug("handleExceptionInternal called for: {}", ex.getClass().getSimpleName());
        
        // If body is already set by our handlers, use it
        if (body instanceof ErrorEntity) {
            return new ResponseEntity<>(body, headers, statusCode);
        }
        
        // Convert any other response to our ErrorEntity format
        HttpStatus httpStatus = HttpStatus.resolve(statusCode.value());
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        ErrorEntity error = new ErrorEntity(
                DateTimeFormatter.ofPattern(DateFormattingConfiguration.APP_DATE_FORMAT).format(ZonedDateTime.now()),
                statusCode.value(),
                httpStatus.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "An error occurred"
        );
        
        return new ResponseEntity<>(error, headers, statusCode);
    }

    // ========== Application-specific exceptions ==========

    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        log.info("Service exception: {}", ex.getMessage());
        return createErrorResponse("Service error", HttpStatus.BAD_REQUEST, ex.getMessage(), "SERVICE_ERROR", request);
    }

    @ExceptionHandler(ControllerException.class)
    protected ResponseEntity<Object> handleControllerException(ControllerException ex, WebRequest request) {
        log.info("Controller exception: {}", ex.getMessage());
        return createErrorResponse("Controller error", HttpStatus.BAD_REQUEST, ex.getMessage(), "CONTROLLER_ERROR", request);
    }

    @ExceptionHandler(EntityException.class)
    protected ResponseEntity<Object> handleEntityException(EntityException ex, WebRequest request) {
        log.info("Entity exception: {}", ex.getMessage());
        return createErrorResponse("Entity error", HttpStatus.BAD_REQUEST, ex.getMessage(), "ENTITY_ERROR", request);
    }

    @ExceptionHandler(EntityNotFound.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFound ex, WebRequest request) {
        log.info("Entity not found: {}", ex.getMessage());
        return createErrorResponse("Not found", HttpStatus.NOT_FOUND, ex.getMessage(), "NOT_FOUND", request);
    }

    // ========== Validation exceptions ==========

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("Constraint violation: {}", message);
        return createErrorResponse("Validation error", HttpStatus.BAD_REQUEST, message, "CONSTRAINT_VIOLATION", request);
    }

    // ========== Security exceptions ==========

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse("Access denied", HttpStatus.FORBIDDEN, 
                "You do not have permission to access this resource", "ACCESS_DENIED", request);
    }

    // ========== Database exceptions ==========

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation", ex);
        return createErrorResponse("Data integrity error", HttpStatus.CONFLICT, 
                "Database constraint violation", "DATA_INTEGRITY_VIOLATION", request);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    protected ResponseEntity<Object> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex, WebRequest request) {
        log.error("Invalid data access", ex);
        return createErrorResponse("Data access error", HttpStatus.BAD_REQUEST, 
                "Error processing request", "INVALID_DATA_ACCESS", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = "Parameter '" + ex.getName() + "' should be of type " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        return createErrorResponse("Type mismatch", HttpStatus.BAD_REQUEST, message, "TYPE_MISMATCH", request);
    }

    // ========== Catch-all ==========

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getClass().getName(), ex);
        return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred", "INTERNAL_ERROR", request);
    }

    // ========== Helper method ==========

    private ResponseEntity<Object> createErrorResponse(String errorName, HttpStatus status, 
                                                        String message, String code, WebRequest request) {
        String path = null;
        if (request instanceof ServletWebRequest servletRequest) {
            path = servletRequest.getRequest().getRequestURI();
        }
        
        ErrorEntity error = new ErrorEntity(
                DateTimeFormatter.ofPattern(DateFormattingConfiguration.APP_DATE_FORMAT).format(ZonedDateTime.now()),
                status.value(),
                errorName,
                message,
                code,
                path
        );
        return new ResponseEntity<>(error, status);
    }
}
