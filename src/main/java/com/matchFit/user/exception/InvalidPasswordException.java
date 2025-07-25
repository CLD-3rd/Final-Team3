package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class InvalidPasswordException extends GeneralException {

	public InvalidPasswordException() {
		super(ErrorCode.PASSWORD_INVALID);
	}
}
