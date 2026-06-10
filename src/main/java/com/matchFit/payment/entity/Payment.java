package com.matchFit.payment.entity;

import com.matchFit.common.entity.BaseEntity;
import com.matchFit.participation.entity.Participation;
import com.matchFit.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participation_id", nullable = false)
    private Participation participation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(unique = true, nullable = false)
    private String paymentKey;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column
    private String cancelReason;

    public static Payment of(Participation participation, Post post,
                             String paymentKey, String orderId, int amount) {
        Payment payment = new Payment();
        payment.participation = participation;
        payment.post = post;
        payment.paymentKey = paymentKey;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.CAPTURED;
        return payment;
    }
}
