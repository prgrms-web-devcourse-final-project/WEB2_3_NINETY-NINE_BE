package com.example.onculture.global.exception;

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

	// 중복 이메일 예외 처리
	@ExceptionHandler(CustomException.DuplicateEmailException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEmailException(CustomException.DuplicateEmailException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.DUPLICATE_EMAIL);
		return ResponseEntity.status(ErrorCode.DUPLICATE_EMAIL.getStatus()).body(errorResponse);
	}

	// 중복 닉네임 예외 처리
	@ExceptionHandler(CustomException.DuplicateNicknameException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateNicknameException(CustomException.DuplicateNicknameException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.DUPLICATE_NICKNAME);
		return ResponseEntity.status(ErrorCode.DUPLICATE_NICKNAME.getStatus()).body(errorResponse);
	}
}
