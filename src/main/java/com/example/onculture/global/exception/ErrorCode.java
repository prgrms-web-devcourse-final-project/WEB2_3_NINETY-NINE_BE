package com.example.onculture.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {


	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

	/*
	 * 204 NO CONTENT
	 */
	NO_CONTENT(HttpStatus.NO_CONTENT, "조회된 데이터가 없습니다."),

	/*
	 * 400 BAD_REQUEST: 잘못된 요청
	 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상, 페이지 크기는 1 이상이어야 합니다."),
	INVALID_PASSWORD_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호를 다시 확인해주새요."),

	INVALID_APPLICATION_STATUS(HttpStatus.BAD_REQUEST, "신청 상태가 유효하지 않습니다."),

	COMPANION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 게시글의 동행이 존재합니다."),


	DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "중복 이메일 입니다."),
	DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "중복 닉네임 입니다."),
	INVALID_GENDER(HttpStatus.BAD_REQUEST, "M OR F"),
	INVALID_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
	PASSWORD_CANNOT_BE_NULL(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요."),
	PASSWORD_INVALID_LENGTH(HttpStatus.BAD_REQUEST, "비밀번호 길이를 확인해주세요."),
	PASSWORD_MISSING_LETTER(HttpStatus.BAD_REQUEST, "비밀번호 글자를 확인해주세요."),
	PASSWORD_MISSING_NUMBER(HttpStatus.BAD_REQUEST, "비밀번호 숫자를 확인해주세요."),
	PASSWORD_MISSING_SPECIAL_CHARACTER(HttpStatus.BAD_REQUEST, "비밀번호 특수문자를 확인해주세요."),

	/*
	 * 401 UNAUTHORIZED
	 */
	UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다. [로그인] 또는 [회원가입] 후 다시 시도해주세요."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효한 토큰이 아닙니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효한 토큰이 아닙니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "올바른 비밀번호가 아닙니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다."),



	/*
	 * 403 FORBIDDEN: 권한 없음
	 */
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
	UNAUTHORIZED_POST_MANAGE(HttpStatus.FORBIDDEN, "작성자 본인만 게시글을 관리할 수 있습니다."),
	COMMENT_UPDATE_AUTHORIZATION_EXCEPTION(HttpStatus.FORBIDDEN, "작성자 본인만 댓글을 수정할 수 있습니다."),
	COMMENT_DELETE_AUTHORIZATION_EXCEPTION(HttpStatus.FORBIDDEN, "작성자 본인 또는 관리자만 댓글을 삭제할 수 있습니다."),

	/*
	 * 404 NOT_FOUND: 리소스를 찾을 수 없음
	 */
	LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "위치 정보를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),

	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),

	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 동행을 찾을 수 없습니다."),

	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 신청을 찾을 수 없습니다."),

	COMPANION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 동행의 동행원이 아닙니다."),


	/*
	 * 405 METHOD_NOT_ALLOWED: 허용되지 않은 Request Method 호출
	 */
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),


	/*
	 * 409 CONFLICT: 사용자의 요청이 서버의 상태와 충돌
	 */
	DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "이미 신청된 사용자입니다."),

	/*
	 * 500 INTERNAL_SERVER_ERROR
	 */
	IMAGE_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
	POST_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 삭제에 실패했습니다.")
	;

	private final HttpStatus status;
	private final String message;
}
