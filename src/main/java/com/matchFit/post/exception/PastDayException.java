package com.matchFit.post.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PastDayException extends GeneralException {
    public PastDayException() {
        super(ErrorCode.POST_PAST_DAYS);
    }
}
