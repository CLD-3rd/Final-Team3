package com.matchFit.payment.service;

import com.matchFit.payment.client.TossPaymentClient;
import com.matchFit.payment.dto.request.TossAuthorizeRequest;
import com.matchFit.payment.dto.response.TossPaymentResponse;
import com.matchFit.payment.entity.Payment;
import com.matchFit.payment.entity.PaymentStatus;
import com.matchFit.payment.repository.PaymentRepository;
import com.matchFit.participation.entity.Participation;
import com.matchFit.post.entity.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;

    /** PG 결제 승인 요청 — 트랜잭션 없이 실행 */
    public TossPaymentResponse authorize(TossAuthorizeRequest request) {
        return tossPaymentClient.authorize(request);
    }

    /** Payment 엔티티 저장 — 호출자의 트랜잭션에 참여 */
    public Payment save(Participation participation, Post post, TossPaymentResponse response) {
        return paymentRepository.save(
                Payment.of(participation, post,
                        response.getPaymentKey(), response.getOrderId(), response.getTotalAmount())
        );
    }

    /** PG 결제 취소(환불) — 트랜잭션 없이 실행 */
    public void voidPayment(String paymentKey, String cancelReason) {
        tossPaymentClient.cancel(paymentKey, cancelReason);
    }

    /**
     * 모집글에 속한 CAPTURED 결제를 모두 환불.
     * 모집글 삭제 시 호출. PG 실패는 로그만 남기고 계속 진행한다.
     */
    public void voidAllCapturedByPost(Long postId) {
        List<Payment> payments = paymentRepository.findByPost_IdAndStatus(postId, PaymentStatus.CAPTURED);
        for (Payment payment : payments) {
            try {
                tossPaymentClient.cancel(payment.getPaymentKey(), "모집글 삭제");
                payment.setStatus(PaymentStatus.CANCELLED);
            } catch (Exception e) {
                log.error("PG 환불 실패 paymentId={}", payment.getId(), e);
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);
        }
    }

    /**
     * 정원 미달로 만료된 모집글의 CAPTURED 결제를 일괄 환불.
     * expirePosts 스케줄러에서 OPEN 게시글 대상으로 실행한다.
     * PG 실패는 FAILED로 기록하고 다음 결제로 넘어간다.
     */
    public void refundByPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) return;
        List<Payment> payments = paymentRepository.findByPost_IdInAndStatus(postIds, PaymentStatus.CAPTURED);
        for (Payment payment : payments) {
            try {
                tossPaymentClient.cancel(payment.getPaymentKey(), "정원 미달 만료");
                payment.setStatus(PaymentStatus.CANCELLED);
            } catch (Exception e) {
                log.error("PG 환불 실패 paymentId={}", payment.getId(), e);
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);
        }
    }
}
