package com.matchFit.post.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class PostNotFoundException : GeneralException(ErrorCode.POST_NOT_FOUND)
