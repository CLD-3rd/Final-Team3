package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class KakaoLoginException extends GeneralException {

	public KakaoLoginException() {
		super(ErrorCode.KAKAO_LOGIN_FAILED);
	}

}
