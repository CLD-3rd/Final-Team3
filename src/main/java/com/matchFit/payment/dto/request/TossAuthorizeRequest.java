package com.matchFit.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossAuthorizeRequest {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
