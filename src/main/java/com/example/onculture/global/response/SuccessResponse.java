package com.example.onculture.global.response;

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

    // 성공 응답 (httpStatus 코드 기입)
    public static <T> SuccessResponse<T> success(HttpStatus httpStatus, T data) {
        return new SuccessResponse<>(true, httpStatus.value(), "성공", data);
    }
}
