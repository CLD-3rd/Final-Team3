package com.matchFit.common.exception

import com.matchFit.common.code.ErrorCode

class GeneralException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
