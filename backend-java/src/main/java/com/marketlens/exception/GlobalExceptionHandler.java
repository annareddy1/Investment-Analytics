package com.marketlens.exception;

import com.marketlens.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.validationError(
                "Request validation failed",
                request.getRequestURI()
        );

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            response.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn(
                "Validation failed on {} with {} field errors",
                request.getRequestURI(),
                response.getDetails().size()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Malformed request body on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.badRequest(
                "Malformed request body. Please check your JSON syntax.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Type mismatch on {}: parameter '{}' received value '{}'",
                request.getRequestURI(),
                ex.getName(),
                ex.getValue()
        );

        ApiErrorResponse response = ApiErrorResponse.badRequest(
                String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()),
                request.getRequestURI()
        );

        response.addFieldError(ex.getName(), "Invalid type or format");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex.getMessage() == null ? "Invalid request" : ex.getMessage();

        if (message.toLowerCase().contains("not found")) {
            ApiErrorResponse response = ApiErrorResponse.notFound(message, request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiErrorResponse response = ApiErrorResponse.badRequest(message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            Exception ex,
            HttpServletRequest request
    ) {
        log.warn("Endpoint not found: {} {}", request.getMethod(), request.getRequestURI());

        ApiErrorResponse response = ApiErrorResponse.notFound(
                String.format("Endpoint not found: %s %s", request.getMethod(), request.getRequestURI()),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("Method not allowed on {}: {}", request.getRequestURI(), ex.getMethod());

        String supportedMethods = ex.getSupportedHttpMethods() == null
                ? ""
                : ex.getSupportedHttpMethods().stream()
                        .map(Object::toString)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("METHOD_NOT_ALLOWED")
                .message(String.format(
                        "HTTP method %s is not supported for this endpoint. Supported methods: %s",
                        ex.getMethod(),
                        supportedMethods
                ))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.unauthorized(
                "Authentication required. Please provide valid credentials.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.forbidden(
                "Access denied. You do not have permission to access this resource.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.error("Illegal state on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.internalError(
                ex.getMessage() == null ? "An unexpected server error occurred." : ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.internalError(
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}