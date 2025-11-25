package com.example.resilience.model;

import java.time.Instant;

/**
 * Generic API response wrapper with metadata.
 */
public record ApiResponse<T>(
        T data,
        String status,
        String message,
        Instant timestamp,
        boolean fromCache,
        boolean fallback
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, "SUCCESS", null, Instant.now(), false, false);
    }

    public static <T> ApiResponse<T> cached(T data) {
        return new ApiResponse<>(data, "SUCCESS", "Served from cache", Instant.now(), true, false);
    }

    public static <T> ApiResponse<T> fallback(T data, String reason) {
        return new ApiResponse<>(data, "DEGRADED", reason, Instant.now(), false, true);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, "ERROR", message, Instant.now(), false, false);
    }
}
