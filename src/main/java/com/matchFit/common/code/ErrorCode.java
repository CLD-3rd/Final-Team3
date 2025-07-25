package com.matchFit.common.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	
	// POST
	POST_UNAUTHORIZED_USER("POST400", "게시글 작성 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
	POST_PAST_EVENT_MODIFICATION("POST401", "과거 날짜에 게시글을 작성할 수 없습니다.", HttpStatus.BAD_REQUEST),
	POST_NOT_FOUND("POST402", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND),
	
	
	// USER
	EMAIL_DUPLICATION("USER400", "이미 사용중인 이메일입니다.", HttpStatus.BAD_REQUEST), 
	NICKNAME_DUPLICATION("USER401", "이미 사용중인 닉네임입니다.", HttpStatus.BAD_REQUEST),
	GENDER_INVALID("USER402", "유효하지 않은 성별입니다.", HttpStatus.BAD_REQUEST),
	SPORTS_INVALID("USER403", "유효하지 않은 운동입니다.", HttpStatus.BAD_REQUEST),
	USER_NOT_FOUND("USER404", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
	PASSWORD_INVALID("USER405", "유효하지 않은 비밀번호입니다.", HttpStatus.BAD_REQUEST),
	KAKAO_LOGIN_FAILED("USER405", "카카오 계정으로 로그인한 사용자입니다. 일반 로그인은 불가능합니다.", HttpStatus.UNAUTHORIZED);
	
	private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
