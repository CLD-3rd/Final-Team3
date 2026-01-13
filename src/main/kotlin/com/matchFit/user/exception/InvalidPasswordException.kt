package com.matchFit.user.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class InvalidPasswordException : GeneralException(ErrorCode.PASSWORD_INVALID)
