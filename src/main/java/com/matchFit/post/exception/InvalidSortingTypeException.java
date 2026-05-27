package com.matchFit.post.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class InvalidSortingTypeException extends GeneralException {
    public InvalidSortingTypeException() {
        super(ErrorCode.INVALID_SORTING_TYPE);
    }
}
