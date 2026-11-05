package com.example.starter.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard envelope for every API response (success and error).
 *
 * <pre>
 * {
 *   "success": true,
 *   "code": "SUCCESS",
 *   "message": "Thành công",
 *   "data": {...},
 *   "errors": null,
 *   "timestamp": "2026-01-01T00:00:00Z"
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        List<FieldError> errors,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "SUCCESS", null, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, "SUCCESS", message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, List<FieldError> errors) {
        return new ApiResponse<>(false, code, message, null, errors, Instant.now());
    }

    public record FieldError(String field, String message) {
    }
}
