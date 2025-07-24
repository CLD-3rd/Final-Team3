package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class EmailAlreadyExistException extends GeneralException {
    public EmailAlreadyExistException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }
}
