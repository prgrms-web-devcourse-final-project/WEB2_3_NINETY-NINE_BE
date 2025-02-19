package com.example.onculture.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	private final ErrorCode errorCode;
	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	/**
     * 중복 이메일 예외
     */
	public static class DuplicateEmailException extends CustomException {
		public DuplicateEmailException() {
			super(ErrorCode.DUPLICATE_EMAIL);
		}
	}

	/**
	 * 중복 닉네임 예외
	 */
	public static class DuplicateNicknameException extends CustomException {
		public DuplicateNicknameException() {
			super(ErrorCode.DUPLICATE_NICKNAME);
		}
	}
}