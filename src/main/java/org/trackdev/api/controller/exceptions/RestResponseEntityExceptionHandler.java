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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        
        ErrorEntity error = createErrorEntity("Method not allowed", HttpStatus.METHOD_NOT_ALLOWED, message, "METHOD_NOT_ALLOWED", request);
        Map<String, Object> details = new HashMap<>();
        details.put("requestedMethod", ex.getMethod());
        if (ex.getSupportedMethods() != null) {
            details.put("supportedMethods", ex.getSupportedMethods());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Media type '" + ex.getContentType() + "' is not supported";
        log.warn("Media type not supported: {}", message);
        
        ErrorEntity error = createErrorEntity("Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, "UNSUPPORTED_MEDIA_TYPE", request);
        Map<String, Object> details = new HashMap<>();
        if (ex.getContentType() != null) {
            details.put("requestedMediaType", ex.getContentType().toString());
        }
        if (!ex.getSupportedMediaTypes().isEmpty()) {
            details.put("supportedMediaTypes", ex.getSupportedMediaTypes().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Media type not acceptable");
        
        ErrorEntity error = createErrorEntity("Not acceptable", HttpStatus.NOT_ACCEPTABLE, 
                "Could not find acceptable representation", "NOT_ACCEPTABLE", request);
        Map<String, Object> details = new HashMap<>();
        if (!ex.getSupportedMediaTypes().isEmpty()) {
            details.put("supportedMediaTypes", ex.getSupportedMediaTypes().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Missing path variable: " + ex.getVariableName();
        log.warn(message);
        
        ErrorEntity error = createErrorEntity("Missing path variable", HttpStatus.BAD_REQUEST, message, "MISSING_PATH_VARIABLE", request);
        Map<String, Object> details = new HashMap<>();
        details.put("variableName", ex.getVariableName());
        details.put("parameterType", ex.getParameter().getParameterType().getSimpleName());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        log.warn(message);
        
        ErrorEntity error = createErrorEntity("Missing parameter", HttpStatus.BAD_REQUEST, message, "MISSING_PARAMETER", request);
        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", ex.getParameterName());
        details.put("parameterType", ex.getParameterType());
        error.setDetails(details);
        
        List<ErrorEntity.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new ErrorEntity.FieldError(ex.getParameterName(), null, "is required", "Required"));
        error.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Required request part '" + ex.getRequestPartName() + "' is missing";
        log.warn(message);
        
        ErrorEntity error = createErrorEntity("Missing request part", HttpStatus.BAD_REQUEST, message, "MISSING_REQUEST_PART", request);
        Map<String, Object> details = new HashMap<>();
        details.put("partName", ex.getRequestPartName());
        error.setDetails(details);
        
        List<ErrorEntity.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new ErrorEntity.FieldError(ex.getRequestPartName(), null, "is required", "Required"));
        error.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Request binding error: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Request binding error", HttpStatus.BAD_REQUEST, ex.getMessage(), "BINDING_ERROR", request);
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getCause() != null) {
            details.put("cause", ex.getCause().getMessage());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        
        List<ErrorEntity.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorEntity.FieldError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage(),
                        error.getCode()
                ))
                .collect(Collectors.toList());
        
        String message = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", message);
        
        ErrorEntity error = createErrorEntity("Validation error", HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR", request);
        error.setFieldErrors(fieldErrors);
        
        Map<String, Object> details = new HashMap<>();
        details.put("errorCount", fieldErrors.size());
        details.put("objectName", ex.getBindingResult().getObjectName());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        
        List<ErrorEntity.FieldError> fieldErrors = new ArrayList<>();
        ex.getAllErrors().forEach(error -> {
            String fieldName = "unknown";
            // Try to extract field name from error codes
            String[] codes = error.getCodes();
            if (codes != null && codes.length > 0) {
                String lastCode = codes[codes.length - 1];
                int lastDot = lastCode.lastIndexOf('.');
                if (lastDot >= 0) {
                    fieldName = lastCode.substring(lastDot + 1);
                }
            }
            fieldErrors.add(new ErrorEntity.FieldError(
                    fieldName,
                    null,
                    error.getDefaultMessage(),
                    codes != null && codes.length > 0 ? codes[0] : null
            ));
        });
        
        String message = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getMessage())
                .collect(Collectors.joining(", "));
        
        if (message.isEmpty()) {
            message = "Request validation failed";
        }
        
        log.warn("Handler method validation failed: {}", message);
        
        ErrorEntity error = createErrorEntity("Validation error", HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR", request);
        if (!fieldErrors.isEmpty()) {
            error.setFieldErrors(fieldErrors);
        }
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        log.warn(message);
        
        ErrorEntity error = createErrorEntity("Not found", HttpStatus.NOT_FOUND, message, "ENDPOINT_NOT_FOUND", request);
        Map<String, Object> details = new HashMap<>();
        details.put("httpMethod", ex.getHttpMethod());
        details.put("requestUrl", ex.getRequestURL());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Resource not found: " + ex.getResourcePath();
        log.warn(message);
        
        ErrorEntity error = createErrorEntity("Not found", HttpStatus.NOT_FOUND, message, "RESOURCE_NOT_FOUND", request);
        Map<String, Object> details = new HashMap<>();
        details.put("resourcePath", ex.getResourcePath());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Async request timeout");
        
        ErrorEntity error = createErrorEntity("Request timeout", HttpStatus.SERVICE_UNAVAILABLE, 
                "The request timed out", "REQUEST_TIMEOUT", request);
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Request body is missing or malformed";
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        
        // Try to get more specific error message
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String fieldPath = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));
            message = "Invalid value for field: " + fieldPath;
            details.put("field", fieldPath);
            details.put("invalidValue", ife.getValue());
            details.put("targetType", ife.getTargetType().getSimpleName());
        } else if (cause instanceof JsonMappingException jme) {
            message = "JSON parsing error: " + jme.getOriginalMessage();
            String fieldPath = jme.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));
            if (!fieldPath.isEmpty()) {
                details.put("field", fieldPath);
            }
        }
        
        log.warn("Message not readable: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Malformed request", HttpStatus.BAD_REQUEST, message, "MALFORMED_REQUEST", request);
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.error("Message not writable: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Internal error", HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error writing response", "WRITE_ERROR", request);
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getCause() != null) {
            details.put("cause", ex.getCause().getClass().getSimpleName());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        log.warn("Max upload size exceeded");
        
        ErrorEntity error = createErrorEntity("File too large", HttpStatus.PAYLOAD_TOO_LARGE, 
                "The uploaded file exceeds the maximum allowed size", "FILE_TOO_LARGE", request);
        Map<String, Object> details = new HashMap<>();
        details.put("maxUploadSize", ex.getMaxUploadSize());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
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
        
        String path = null;
        if (request instanceof ServletWebRequest servletRequest) {
            path = servletRequest.getRequest().getRequestURI();
        }
        
        ErrorEntity error = new ErrorEntity(
                DateTimeFormatter.ofPattern(DateFormattingConfiguration.APP_DATE_FORMAT).format(ZonedDateTime.now()),
                statusCode.value(),
                httpStatus.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "An error occurred",
                "UNHANDLED_SPRING_EXCEPTION",
                path
        );
        
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, headers, statusCode);
    }

    // ========== Application-specific exceptions ==========

    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        log.info("Service exception: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Service error", HttpStatus.BAD_REQUEST, ex.getMessage(), 
                ex.getErrorCode() != null ? ex.getErrorCode() : "SERVICE_ERROR", request);
        
        // Merge exception details with base details
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getDetails() != null) {
            details.putAll(ex.getDetails());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ControllerException.class)
    protected ResponseEntity<Object> handleControllerException(ControllerException ex, WebRequest request) {
        log.info("Controller exception: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Controller error", HttpStatus.BAD_REQUEST, ex.getMessage(), 
                ex.getErrorCode() != null ? ex.getErrorCode() : "CONTROLLER_ERROR", request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getDetails() != null) {
            details.putAll(ex.getDetails());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityException.class)
    protected ResponseEntity<Object> handleEntityException(EntityException ex, WebRequest request) {
        log.info("Entity exception: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Entity error", HttpStatus.BAD_REQUEST, ex.getMessage(), 
                ex.getErrorCode() != null ? ex.getErrorCode() : "ENTITY_ERROR", request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getDetails() != null) {
            details.putAll(ex.getDetails());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFound.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFound ex, WebRequest request) {
        log.info("Entity not found: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Not found", HttpStatus.NOT_FOUND, ex.getMessage(), 
                ex.getErrorCode() != null ? ex.getErrorCode() : "NOT_FOUND", request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getDetails() != null) {
            details.putAll(ex.getDetails());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ========== Validation exceptions ==========

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ErrorEntity.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorEntity.FieldError(
                        extractPropertyPath(violation),
                        violation.getInvalidValue(),
                        violation.getMessage(),
                        extractConstraintCode(violation)
                ))
                .collect(Collectors.toList());
        
        String message = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Constraint violation: {}", message);
        
        ErrorEntity error = createErrorEntity("Validation error", HttpStatus.BAD_REQUEST, message, "CONSTRAINT_VIOLATION", request);
        error.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    private String extractPropertyPath(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        // Extract just the field name if it's nested (e.g., "methodName.paramName" -> "paramName")
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
    
    private String extractConstraintCode(ConstraintViolation<?> violation) {
        String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        return annotationType;
    }

    // ========== Security exceptions ==========

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorEntity error = createErrorEntity("Access denied", HttpStatus.FORBIDDEN, 
                "You do not have permission to access this resource", "ACCESS_DENIED", request);
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // ========== Database exceptions ==========

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation", ex);
        
        ErrorEntity error = createErrorEntity("Data integrity error", HttpStatus.CONFLICT, 
                "Database constraint violation", "DATA_INTEGRITY_VIOLATION", request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        
        // Try to extract more meaningful information from the root cause
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            String rootMessage = rootCause.getMessage();
            details.put("rootCause", rootCause.getClass().getSimpleName());
            
            // Parse common database constraint messages
            if (rootMessage != null) {
                if (rootMessage.contains("Duplicate entry")) {
                    error.setMessage("A record with this value already exists");
                    details.put("constraintType", "UNIQUE");
                } else if (rootMessage.contains("foreign key constraint")) {
                    error.setMessage("Referenced record does not exist or cannot be deleted");
                    details.put("constraintType", "FOREIGN_KEY");
                } else if (rootMessage.contains("cannot be null")) {
                    error.setMessage("Required field is missing");
                    details.put("constraintType", "NOT_NULL");
                }
            }
        }
        
        error.setDetails(details);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    protected ResponseEntity<Object> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex, WebRequest request) {
        log.error("Invalid data access", ex);
        
        ErrorEntity error = createErrorEntity("Data access error", HttpStatus.BAD_REQUEST, 
                "Error processing request", "INVALID_DATA_ACCESS", request);
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getCause() != null) {
            details.put("cause", ex.getCause().getClass().getSimpleName());
        }
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = "Parameter '" + ex.getName() + "' should be of type " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        
        ErrorEntity error = createErrorEntity("Type mismatch", HttpStatus.BAD_REQUEST, message, "TYPE_MISMATCH", request);
        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", ex.getName());
        details.put("invalidValue", ex.getValue());
        if (ex.getRequiredType() != null) {
            details.put("requiredType", ex.getRequiredType().getSimpleName());
        }
        error.setDetails(details);
        
        List<ErrorEntity.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new ErrorEntity.FieldError(
                ex.getName(), 
                ex.getValue(), 
                "should be of type " + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"),
                "TypeMismatch"
        ));
        error.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ========== Catch-all ==========

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getClass().getName(), ex);
        
        ErrorEntity error = createErrorEntity("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred", "INTERNAL_ERROR", request);
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        error.setDetails(details);
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== Helper methods ==========

    private ResponseEntity<Object> createErrorResponse(String errorName, HttpStatus status, 
                                                        String message, String code, WebRequest request) {
        ErrorEntity error = createErrorEntity(errorName, status, message, code, request);
        return new ResponseEntity<>(error, status);
    }
    
    private ErrorEntity createErrorEntity(String errorName, HttpStatus status, 
                                           String message, String code, WebRequest request) {
        String path = null;
        if (request instanceof ServletWebRequest servletRequest) {
            path = servletRequest.getRequest().getRequestURI();
        }
        
        return new ErrorEntity(
                DateTimeFormatter.ofPattern(DateFormattingConfiguration.APP_DATE_FORMAT).format(ZonedDateTime.now()),
                status.value(),
                errorName,
                message,
                code,
                path
        );
    }
}
