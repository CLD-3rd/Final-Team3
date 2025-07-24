package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class SportsInvalidException extends GeneralException {
	
	public SportsInvalidException() {
		super(ErrorCode.SPORTS_INVALID);
	}

}
