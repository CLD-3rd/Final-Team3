package com.matchFit.common.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	EMAIL_DUPLICATION("USER400", "이미 사용중인 이메일입니다.", HttpStatus.BAD_REQUEST);
	
	private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
