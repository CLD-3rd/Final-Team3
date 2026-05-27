package com.matchFit.user.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class NicknameAlreadyExistException extends GeneralException {
    public NicknameAlreadyExistException() {
        super(ErrorCode.NICKNAME_DUPLICATION);
    }
}
