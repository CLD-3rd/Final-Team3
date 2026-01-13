package com.matchFit.user.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class NicknameAlreadyExistException : GeneralException(ErrorCode.NICKNAME_DUPLICATION)
