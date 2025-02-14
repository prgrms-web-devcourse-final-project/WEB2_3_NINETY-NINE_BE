package com.example.onculture.exception;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ErrorResponse {
	private final LocalDateTime timestamp;
	private final int status;
	private final String error;
	private final String errorCode;

	public ErrorResponse(ErrorCode errorCode) {
		this.timestamp = LocalDateTime.now();
		this.status = errorCode.getStatus().value();
		this.error = errorCode.getMessage();
		this.errorCode = errorCode.name();
	}
}
