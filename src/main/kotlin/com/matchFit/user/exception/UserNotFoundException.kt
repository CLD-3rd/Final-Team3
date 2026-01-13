package com.matchFit.user.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class UserNotFoundException : GeneralException(ErrorCode.USER_NOT_FOUND)
