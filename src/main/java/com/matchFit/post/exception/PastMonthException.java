package com.matchFit.post.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PastMonthException extends GeneralException {

	public PastMonthException() {
		super(ErrorCode.POST_PAST_MONTHS);
	}
}
