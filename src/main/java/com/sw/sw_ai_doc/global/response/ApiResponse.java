package com.sw.sw_ai_doc.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    public static ApiResponse<Void> success(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
