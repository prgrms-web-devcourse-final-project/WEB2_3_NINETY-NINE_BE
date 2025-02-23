package com.example.onculture.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	private final ErrorCode errorCode;
	private String massage;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}


	// 커스텀 JPA Save 예외 클래스 (try~catch 구문에서 원하는 에러코드 지정 가능)
	public static class CustomJpaSaveException extends CustomException {
		public CustomJpaSaveException(ErrorCode errorCode) {
			super(errorCode);
		}
	}

	// 커스텀 JPA Delete 예외 클래스 (try~catch 구문에서 원하는 에러코드 지정 가능)
	public static class CustomJpaDeleteException extends CustomException {
		public CustomJpaDeleteException(ErrorCode errorCode) {
			super(errorCode);
		}
	}

	// 커스텀 토큰 인증 예외 클래스
	public static class CustomInvalidTokenException extends CustomException {
		public CustomInvalidTokenException(ErrorCode errorCode) {
			super(errorCode);
		}
	}

	// 중복 이메일 예외
	public static class DuplicateEmailException extends CustomException {
		public DuplicateEmailException() {
			super(ErrorCode.DUPLICATE_EMAIL);
		}
	}

	// 중복 닉네임 예외
	public static class DuplicateNicknameException extends CustomException {
		public DuplicateNicknameException() {
			super(ErrorCode.DUPLICATE_NICKNAME);
		}
	}

	// 로그인하지 않은 사용자 접근 시
	public static class CustomAuthenticationException extends CustomException {
		public CustomAuthenticationException() {
			super(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN);
		}
	}

	// 로그인했지만 권한 부족 시
	public static class CustomAccessDeniedException extends CustomException {
		public CustomAccessDeniedException() {
			super(ErrorCode.ACCESS_DENIED);
		}
	}
}