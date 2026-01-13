package com.matchFit.user.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class KakaoLoginException : GeneralException(ErrorCode.KAKAO_LOGIN_FAILED)
