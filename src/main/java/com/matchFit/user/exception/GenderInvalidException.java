package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class GenderInvalidException extends GeneralException {
    
	public GenderInvalidException() {
        super(ErrorCode.GENDER_INVALID);
    }
}
