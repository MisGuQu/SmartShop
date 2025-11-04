package com.smartshop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for consistent response format
 * @param <T> Type of the response data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Create a successful response with data
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, null);
    }

    /**
     * Create a successful response with data and message
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Create a successful response with status and data
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
        return new ResponseEntity<>(response, status);
    }

    /**
     * Create an error response with message
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Create an error response with message and status
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
