package com.matchFit.user.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class EmailAlreadyExistException : GeneralException(ErrorCode.EMAIL_DUPLICATION)
