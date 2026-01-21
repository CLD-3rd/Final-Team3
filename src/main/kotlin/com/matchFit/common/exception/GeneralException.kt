package com.matchFit.common.exception

import com.matchFit.common.code.ErrorCode

open class GeneralException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
