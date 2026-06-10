package com.matchFit.payment.client;

import com.matchFit.payment.dto.request.TossAuthorizeRequest;
import com.matchFit.payment.dto.response.TossPaymentResponse;
import com.matchFit.payment.exception.PaymentAuthorizationFailedException;
import com.matchFit.payment.exception.PaymentCaptureFailedException;
import com.matchFit.payment.exception.PaymentVoidFailedException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TossPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentClient.class);

    private final RestClient restClient;

    public TossPaymentClient(
            @Value("${toss.payments.base-url}") String baseUrl,
            @Value("${toss.payments.secret-key}") String secretKey
    ) {
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encoded)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TossPaymentResponse authorize(TossAuthorizeRequest request) {
        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (Exception e) {
            log.error("Toss authorize failed: {}", e.getMessage(), e);
            throw new PaymentAuthorizationFailedException();
        }
    }

    public void capture(String paymentKey) {
        // Toss 선승인 후 매입(capture) 호출. 표준 결제는 confirm 시점에 자동 매입되므로
        // 선승인 결제(pre-auth) 상품을 사용하는 경우 실제 매입 API 경로로 교체한다.
        try {
            restClient.post()
                    .uri("/v1/payments/{paymentKey}/capture", paymentKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Toss capture failed for paymentKey={}: {}", paymentKey, e.getMessage(), e);
            throw new PaymentCaptureFailedException();
        }
    }

    public void cancel(String paymentKey, String cancelReason) {
        try {
            restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(Map.of("cancelReason", cancelReason))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Toss cancel failed for paymentKey={}: {}", paymentKey, e.getMessage(), e);
            throw new PaymentVoidFailedException();
        }
    }
}
