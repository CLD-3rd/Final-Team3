package com.matchFit.payment.repository;

import com.matchFit.payment.entity.Payment;
import com.matchFit.payment.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByParticipation_IdAndStatus(Long participationId, PaymentStatus status);

    List<Payment> findByPost_IdAndStatus(Long postId, PaymentStatus status);

    List<Payment> findByPost_IdInAndStatus(List<Long> postIds, PaymentStatus status);

    @Transactional
    void deleteByPost_Id(Long postId);
}
