package com.matchFit.participation.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class ParticipationCancellationTimeExceededException extends GeneralException {
    public ParticipationCancellationTimeExceededException() {
        super(ErrorCode.PARTICIPATION_CANCELLATION_TIME_EXCEEDED);
    }
}
