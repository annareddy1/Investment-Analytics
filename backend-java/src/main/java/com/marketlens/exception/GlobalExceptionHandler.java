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

/**
 * ✅ Global Exception Handler - Production-Grade Error Handling
 *
 * Returns consistent error responses in this format:
 * {
 *   "error": "VALIDATION_ERROR|NOT_FOUND|INTERNAL_ERROR|...",
 *   "message": "Human-readable description",
 *   "details": [ { "field": "ticker", "issue": "required" } ],
 *   "timestamp": "2026-02-11T20:00:00",
 *   "path": "/api/analysis/run"
 * }
 *
 * Handles:
 * - Validation errors (400)
 * - Not found (404)
 * - Method not allowed (405)
 * - Authentication errors (401)
 * - Authorization errors (403)
 * - Internal errors (500)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (Bean Validation)
     * Returns: 400 BAD REQUEST with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.validationError(
                "Request validation failed",
                request.getRequestURI()
        );

        // Add field-level error details
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            response.addFieldError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        log.warn("Validation error on {}: {} fields failed",
                request.getRequestURI(), response.getDetails().size());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle malformed JSON or missing request body
     * Returns: 400 BAD REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Malformed request body on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.badRequest(
                "Malformed request body. Please check your JSON syntax.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle illegal arguments (e.g., invalid UUID, enum values)
     * Returns: 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

        // Check if it's a "not found" error message
        String message = ex.getMessage();
        if (message != null && (message.contains("not found") || message.contains("Not found"))) {
            ApiErrorResponse response = ApiErrorResponse.notFound(message, request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiErrorResponse response = ApiErrorResponse.badRequest(
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle type mismatch (e.g., passing string where UUID expected)
     * Returns: 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch on {}: parameter '{}' has invalid value '{}'",
                request.getRequestURI(), ex.getName(), ex.getValue());

        ApiErrorResponse response = ApiErrorResponse.badRequest(
                String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()),
                request.getRequestURI()
        );

        response.addFieldError(ex.getName(), "Invalid type or format");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle 404 Not Found (no handler found for request)
     * Returns: 404 NOT FOUND
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            Exception ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} {}", request.getMethod(), request.getRequestURI());

        ApiErrorResponse response = ApiErrorResponse.notFound(
                String.format("Endpoint not found: %s %s", request.getMethod(), request.getRequestURI()),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle 405 Method Not Allowed
     * Returns: 405 METHOD NOT ALLOWED
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Method not allowed: {} on {}", ex.getMethod(), request.getRequestURI());

        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("")
                : "";

        String message = String.format(
                "HTTP method %s is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(),
                supportedMethods
        );

        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("METHOD_NOT_ALLOWED")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Handle authentication failures (JWT validation, missing auth)
     * Returns: 401 UNAUTHORIZED
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.unauthorized(
                "Authentication required. Please provide valid credentials.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle access denied (insufficient permissions)
     * Returns: 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.forbidden(
                "Access denied. You do not have permission to access this resource.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle generic runtime exceptions
     * Returns: 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Runtime exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.internalError(
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Catch-all for any unhandled exceptions
     * Returns: 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.internalError(
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
