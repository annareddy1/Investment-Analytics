package com.marketlens.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Standardized API Error Response
 * - Consistent error format across all endpoints
 * - Includes validation details when applicable
 * - Machine-readable error codes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * Error code for programmatic handling
     * Values: VALIDATION_ERROR, NOT_FOUND, INTERNAL_ERROR, BAD_REQUEST, UNAUTHORIZED, FORBIDDEN
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Detailed field-level errors (for validation failures)
     */
    @Builder.Default
    private List<FieldError> details = new ArrayList<>();

    /**
     * Timestamp when error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Field-level validation error
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String issue;
    }

    /**
     * Add a field error
     */
    public void addFieldError(String field, String issue) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(new FieldError(field, issue));
    }

    /**
     * Factory method for validation errors
     */
    public static ApiErrorResponse validationError(String message, String path) {
        return ApiErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Factory method for not found errors
     */
    public static ApiErrorResponse notFound(String message, String path) {
        return ApiErrorResponse.builder()
                .error("NOT_FOUND")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    /**
     * Factory method for internal errors
     */
    public static ApiErrorResponse internalError(String message, String path) {
        return ApiErrorResponse.builder()
                .error("INTERNAL_ERROR")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    /**
     * Factory method for bad request errors
     */
    public static ApiErrorResponse badRequest(String message, String path) {
        return ApiErrorResponse.builder()
                .error("BAD_REQUEST")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    /**
     * Factory method for unauthorized errors
     */
    public static ApiErrorResponse unauthorized(String message, String path) {
        return ApiErrorResponse.builder()
                .error("UNAUTHORIZED")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    /**
     * Factory method for forbidden errors
     */
    public static ApiErrorResponse forbidden(String message, String path) {
        return ApiErrorResponse.builder()
                .error("FORBIDDEN")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }
}
