package com.matchFit.post.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class UnauthorizedUserException : GeneralException(ErrorCode.POST_UNAUTHORIZED_USER)
