package com.marketlens.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private String error;
    private String message;

    @Builder.Default
    private List<FieldError> details = new ArrayList<>();

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String issue;
    }

    public void addFieldError(String field, String issue) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(new FieldError(field, issue));
    }

    public static ApiErrorResponse validationError(String message, String path) {
        return ApiErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message(message)
                .path(path)
                .build();
    }

    public static ApiErrorResponse notFound(String message, String path) {
        return ApiErrorResponse.builder()
                .error("NOT_FOUND")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    public static ApiErrorResponse internalError(String message, String path) {
        return ApiErrorResponse.builder()
                .error("INTERNAL_ERROR")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    public static ApiErrorResponse badRequest(String message, String path) {
        return ApiErrorResponse.builder()
                .error("BAD_REQUEST")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    public static ApiErrorResponse unauthorized(String message, String path) {
        return ApiErrorResponse.builder()
                .error("UNAUTHORIZED")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }

    public static ApiErrorResponse forbidden(String message, String path) {
        return ApiErrorResponse.builder()
                .error("FORBIDDEN")
                .message(message)
                .path(path)
                .details(null)
                .build();
    }
}