package com.matchFit.payment.exception;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;

public class PaymentAuthorizationFailedException extends GeneralException {
    public PaymentAuthorizationFailedException() {
        super(ErrorCode.PAYMENT_AUTHORIZATION_FAILED);
    }
}
