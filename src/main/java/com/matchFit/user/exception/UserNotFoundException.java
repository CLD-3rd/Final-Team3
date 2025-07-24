package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class UserNotFoundException extends GeneralException {

	public UserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}


}
