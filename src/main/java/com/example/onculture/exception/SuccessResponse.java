package com.example.onculture.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class SuccessResponse<T> {
    private boolean isSuccess;
    private int code;
    private String message;
    private T data;

    // 성공 응답 (기본 메시지)
    public static <T> SuccessResponse<T> success(T data) {
        return new SuccessResponse<>(true, HttpStatus.OK.value(), "성공", data);
    }

    // 성공 응답 (커스텀 메시지)
    public static <T> SuccessResponse<T> success(String message, T data) {
        return new SuccessResponse<>(true, HttpStatus.OK.value(), message, data);
    }

    // 성공 응답 (httpStatus 코드 종류 다양하게 할 시 사용)
    public static <T> SuccessResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new SuccessResponse<>(true, httpStatus.value(), message, data);
    }

    // 에러 응답
//    public static ApiResponse<Void> error(HttpStatus httpStatus) {
//        return new ApiResponse<>(false, httpStatus.value(), "실패", null);
//    }
//
//    public static ApiResponse<Void> error(HttpStatus httpStatus, String message) {
//        return new ApiResponse<>(false, httpStatus.value(), message, null);
//    }
//
//    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message, T data) {
//        return new ApiResponse<>(false, httpStatus.value(), message, null);
//    }
}
