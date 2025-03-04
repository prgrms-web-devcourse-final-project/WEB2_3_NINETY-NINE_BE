package com.example.onculture.global.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus()).body(new ErrorResponse(errorCode));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_INPUT_VALUE);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(errorResponse);
	}

	// 커스텀 JPA Save 예외 핸들러 ( try~catch 구문에서 원하는 에러코드 지정 가능 )
	// 사용방법 : throw new CustomException.CustomJpaSaveException(ErrorCode.사용할 에러코드);
	@ExceptionHandler(CustomException.CustomJpaSaveException.class)
	public ResponseEntity<ErrorResponse> handleCustomJpaSaveException(CustomException.CustomJpaSaveException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode());
		return ResponseEntity.status(ex.getErrorCode().getStatus()).body(errorResponse);
	}

	// 커스텀 JPA Read 예외 핸들러 ( try~catch 구문에서 원하는 에러코드 지정 가능 )
	@ExceptionHandler(CustomException.CustomJpaReadException.class)
	public ResponseEntity<ErrorResponse> handleCustomJpaReadException(CustomException.CustomJpaReadException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode());
		return ResponseEntity.status(ex.getErrorCode().getStatus()).body(errorResponse);
	}

	// 커스텀 JPA Delete 예외 핸들러 ( try~catch 구문에서 원하는 에러코드 지정 가능 )
	@ExceptionHandler(CustomException.CustomJpaDeleteException.class)
	public ResponseEntity<ErrorResponse> handleCustomJpaDeleteException(CustomException.CustomJpaDeleteException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode());
		return ResponseEntity.status(ex.getErrorCode().getStatus()).body(errorResponse);
	}

	// 커스텀 토큰 인증 예외 핸들러 ( try~catch 구문에서 원하는 에러코드 지정 가능 )
	@ExceptionHandler(CustomException.CustomInvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleCustomInvalidTokenException(CustomException.CustomInvalidTokenException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode());
		return ResponseEntity.status(ex.getErrorCode().getStatus()).body(errorResponse);
	}

	// user 도메인 - 리팩토링 에정 핸들러
	// 사용자 인증 과정 - 사용자가 없을 경우
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.USER_NOT_FOUND);
		return ResponseEntity.status(ErrorCode.USER_NOT_FOUND.getStatus()).body(errorResponse);
	}

	// 사용자 인증 과정 - 비밀번호가 일치하지 않을 경우 ( 비밀번호가 일치하지 않을 경우, BadCredentialsException 에러가 발생한다 )
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_CREDENTIALS);
		return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getStatus()).body(errorResponse);
	}

	// 중복 이메일 예외 핸들러
	@ExceptionHandler(CustomException.DuplicateEmailException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEmailException(CustomException.DuplicateEmailException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.DUPLICATE_EMAIL);
		return ResponseEntity.status(ErrorCode.DUPLICATE_EMAIL.getStatus()).body(errorResponse);
	}

	// 중복 닉네임 예외 핸들러
	@ExceptionHandler(CustomException.DuplicateNicknameException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateNicknameException(CustomException.DuplicateNicknameException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.DUPLICATE_NICKNAME);
		return ResponseEntity.status(ErrorCode.DUPLICATE_NICKNAME.getStatus()).body(errorResponse);
	}

	// 로그인하지 않은 사용자 접근 예외 핸들러
	@ExceptionHandler(CustomException.CustomAuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(CustomException.CustomAuthenticationException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN);
		return ResponseEntity.status(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN.getStatus()).body(errorResponse);
	}

	// 로그인했지만 권한 부족 예외 핸들러
	@ExceptionHandler(CustomException.CustomAccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleCustomAccessDeniedException(CustomException.CustomAccessDeniedException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.ACCESS_DENIED);
		return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getStatus()).body(errorResponse);
	}

	// 토큰 유효성 검증 - 토큰 만료 예외 핸들러
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.TOKEN_EXPIRED);
		return ResponseEntity.status(ErrorCode.TOKEN_EXPIRED.getStatus()).body(errorResponse);
	}

	// 토큰 유효성 검증 - 토큰 유효성 x 예외 핸들러
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_TOKEN);
		return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getStatus()).body(errorResponse);
	}
}
