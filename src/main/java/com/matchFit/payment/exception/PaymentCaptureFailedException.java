package com.matchFit.payment.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PaymentCaptureFailedException extends GeneralException {
    public PaymentCaptureFailedException() {
        super(ErrorCode.PAYMENT_CAPTURE_FAILED);
    }
}
