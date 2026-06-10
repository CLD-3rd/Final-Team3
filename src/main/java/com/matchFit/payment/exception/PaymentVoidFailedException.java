package com.matchFit.payment.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PaymentVoidFailedException extends GeneralException {
    public PaymentVoidFailedException() {
        super(ErrorCode.PAYMENT_VOID_FAILED);
    }
}
