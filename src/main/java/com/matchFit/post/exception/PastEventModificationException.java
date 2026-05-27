package com.matchFit.post.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PastEventModificationException extends GeneralException {
    public PastEventModificationException() {
        super(ErrorCode.POST_PAST_EVENT_MODIFICATION);
    }
}
