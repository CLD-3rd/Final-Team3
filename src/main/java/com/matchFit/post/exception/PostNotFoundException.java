package com.matchFit.post.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PostNotFoundException extends GeneralException {

	public PostNotFoundException() {
		super(ErrorCode.POST_NOT_FOUND);
	}

}
