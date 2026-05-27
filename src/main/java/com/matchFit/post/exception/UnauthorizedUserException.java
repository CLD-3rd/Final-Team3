package com.matchFit.post.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class UnauthorizedUserException extends GeneralException {
    public UnauthorizedUserException() {
        super(ErrorCode.POST_UNAUTHORIZED_USER);
    }
}
