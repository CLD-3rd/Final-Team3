package com.matchFit.post.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class MissingViewerKeyException : GeneralException(ErrorCode.POST_VIEWER_KEY_REQUIRED)
