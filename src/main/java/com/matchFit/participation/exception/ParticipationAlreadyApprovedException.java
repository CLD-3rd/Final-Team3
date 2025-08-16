package com.matchFit.participation.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class ParticipationAlreadyApprovedException extends GeneralException {
    public ParticipationAlreadyApprovedException() {
        super(ErrorCode.PARTICIPATION_ALREADY_APPROVED);
    }
}
