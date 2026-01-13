package com.matchFit.participation.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException

class ParticipationCancellationTimeExceededException :
    GeneralException(ErrorCode.PARTICIPATION_CANCELLATION_TIME_EXCEEDED)
