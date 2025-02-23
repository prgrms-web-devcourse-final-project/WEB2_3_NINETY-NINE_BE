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
	INVALID_SORT_REQUEST(HttpStatus.BAD_REQUEST, "정렬은 popular, latest, comments만 가능합니다."),
	INVALID_PASSWORD_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호를 다시 확인해주새요."),

	INVALID_APPLICATION_STATUS(HttpStatus.BAD_REQUEST, "신청 상태가 유효하지 않습니다."),

	COMPANION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 게시글의 동행이 존재합니다."),
	COMMENT_NOT_BELONG_TO_POST(HttpStatus.BAD_REQUEST, "해당 댓글은 지정된 포스트에 속하지 않습니다."),

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
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효한 리프레시 토큰이 아닙니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효한 토큰이 아닙니다."),
	UNAUTHORIZED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),		// 로그인하지 않은 사용자 접근 시
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 RefreshToken 토큰 입니다. 재로그인 해주세요."),
	UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "알림에 접근할 권한이 없습니다."),



	/*
	 * 403 FORBIDDEN: 권한 없음
	 */
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
	UNAUTHORIZED_POST_MANAGE(HttpStatus.FORBIDDEN, "작성자 본인만 게시글을 관리할 수 있습니다."),
	UNAUTHORIZED_COMMENT_MANAGE(HttpStatus.FORBIDDEN, "작성자 본인만 댓글을 관리할 수 있습니다."),
	COMMENT_UPDATE_AUTHORIZATION_EXCEPTION(HttpStatus.FORBIDDEN, "작성자 본인만 댓글을 수정할 수 있습니다."),
	COMMENT_DELETE_AUTHORIZATION_EXCEPTION(HttpStatus.FORBIDDEN, "작성자 본인 또는 관리자만 댓글을 삭제할 수 있습니다."),

	/*
	 * 404 NOT_FOUND: 리소스를 찾을 수 없음
	 */
	LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "위치 정보를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),

	/*
	 * 405 METHOD_NOT_ALLOWED: 허용되지 않은 Request Method 호출
	 */
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),


	/*
	 * 409 CONFLICT: 사용자의 요청이 서버의 상태와 충돌
	 */


	/*
	 * 500 INTERNAL_SERVER_ERROR
	 */
	// JPA 조회 실패 시
	FIND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "조회에 실패했습니다."), // 조회 실패 공용 에러코드
	POST_FIND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 조회에 실패했습니다."),
	COMMENT_FIND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 조회에 실패했습니다."),
	USER_FIND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 조회에 실패했습니다."),
	REFRESH_TOKEN_FIND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 조회에 실패했습니다."),
	// JPA save 실패 시
	SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "저장에 실패했습니다."),		// 저장 실패 공용 에러코드
	POST_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 저장에 실패했습니다."),
	COMMENT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 저장에 실패했습니다."),
	USER_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 저장에 실패했습니다."),
	REFRESH_TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 저장에 실패했습니다."),
	// JPA delete 실패 시
	DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "삭제에 실패했습니다."),     // 삭제 실패 공용 에러코드
	POST_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 삭제에 실패했습니다."),
	COMMENT_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 삭제에 실패했습니다."),
	USER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 삭제에 실패했습니다."),
	REFRESH_TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 삭제에 실패했습니다."),
	IMAGE_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
	;

	private final HttpStatus status;
	private final String message;
}
