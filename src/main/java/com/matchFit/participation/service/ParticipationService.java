package com.matchFit.participation.service;

import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.exception.ParticipationCancellationTimeExceededException;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.payment.dto.request.TossAuthorizeRequest;
import com.matchFit.payment.dto.response.TossPaymentResponse;
import com.matchFit.payment.entity.Payment;
import com.matchFit.payment.entity.PaymentStatus;
import com.matchFit.payment.repository.PaymentRepository;
import com.matchFit.payment.service.PaymentService;
import com.matchFit.post.dto.response.GetMyPostApplicant;
import com.matchFit.post.dto.response.GetMyPostApplicants;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;
import com.matchFit.post.exception.PostNotFoundException;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.user.exception.UserNotFoundException;
import com.matchFit.user.repository.UserRepository;
import com.matchFit.user.security.CustomUserDetails;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private static final Logger log = LoggerFactory.getLogger(ParticipationService.class);

    private static final String APPLICANT_KEY_FMT = "applicants:post_%d";
    private static final int APPLICANT_KEY_TTL_MINUTES = 5;

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final StringRedisTemplate redisTemplate;
    private final PlatformTransactionManager transactionManager;

    private String getApplicantKey(Long postId) {
        return String.format(APPLICANT_KEY_FMT, postId);
    }

    private Long updateApplicantCount(Long postId, long delta) {
        String key = getApplicantKey(postId);
        try {
            Duration ttl = Duration.ofMinutes(APPLICANT_KEY_TTL_MINUTES);
            Boolean hasKey = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                Long newCount = redisTemplate.opsForValue().increment(key, delta);
                redisTemplate.expire(key, ttl);
                return newCount;
            }
            int approvedCount = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
            redisTemplate.opsForValue().set(key, Integer.toString(approvedCount), ttl);
            return (long) approvedCount;
        } catch (Exception ex) {
            log.error("Redis update failed for post {}: {}", postId, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 모집글 신청.
     * <p>트랜잭션 원칙: PG 승인(authorize)은 트랜잭션 밖에서 먼저 실행하고,
     * DB 저장(Participation + Payment)은 단일 트랜잭션으로 묶는다.
     * DB 저장 실패 시 PG void로 보상한다.</p>
     */
    public void applyPost(Long postId, Long userId, TossAuthorizeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if (participationRepository.findByPostIdAndUserId(postId, userId) != null) {
            throw new IllegalStateException("이미 신청한 모집글입니다");
        }
        int currentPeople = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
        if (currentPeople >= post.getMaxPeople()) {
            throw new IllegalStateException("마감되었습니다");
        }

        // 1. PG 승인 — 트랜잭션 없음
        TossPaymentResponse pgResp = paymentService.authorize(request);

        // 2. Participation + Payment 원자적 저장
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        try {
            tx.executeWithoutResult(status -> {
                Participation saved = participationRepository.saveAndFlush(new Participation(user, post));
                paymentService.save(saved, post, pgResp);

                int count = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
                if (count >= post.getMaxPeople() && post.getStatus() != Status.CLOSED) {
                    post.setStatus(Status.CLOSED);
                    postRepository.save(post);
                }
            });
        } catch (Exception e) {
            // 3. DB 저장 실패 → PG void 보상
            try {
                paymentService.voidPayment(pgResp.getPaymentKey(), "DB 저장 실패");
            } catch (Exception voidEx) {
                log.error("보상 void 실패 paymentKey={}", pgResp.getPaymentKey(), voidEx);
            }
            throw e;
        }

        // 4. Redis 갱신 (트랜잭션 커밋 후)
        updateApplicantCount(postId, 1L);
    }

    /**
     * 모집글 신청 취소.
     * <p>DB 삭제를 트랜잭션으로 먼저 처리한 뒤 PG void를 호출한다.
     * PG void 실패 시 로그만 남긴다(수동 정산 필요).</p>
     */
    public void cancelApplyPost(Long postId, Long userId) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Participation participation = participationRepository.findByPostIdAndUserId(postId, userId);
        Objects.requireNonNull(participation, "참가 신청 내역이 없습니다");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = post.getDate().toLocalDate().minusDays(1).atStartOfDay();
        if (now.isAfter(cutoffTime)) {
            throw new ParticipationCancellationTimeExceededException();
        }

        Optional<Payment> paymentOpt = paymentRepository.findByParticipation_IdAndStatus(
                participation.getId(), PaymentStatus.CAPTURED);
        String paymentKey = paymentOpt.map(Payment::getPaymentKey).orElse(null);

        // 1. DB 삭제 — 단일 트랜잭션 (Payment → Participation 순서로 FK 제약 준수)
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            paymentOpt.ifPresent(payment -> {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setCancelReason("신청 취소");
                paymentRepository.save(payment);
                paymentRepository.delete(payment);
            });
            participationRepository.delete(participation);

            int currentApproved = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
            if (currentApproved < post.getMaxPeople() && post.getStatus() == Status.CLOSED) {
                post.setStatus(Status.OPEN);
                postRepository.save(post);
            }
        });

        // 2. PG void — 트랜잭션 커밋 후 (실패해도 DB는 이미 취소 완료)
        if (paymentKey != null) {
            try {
                paymentService.voidPayment(paymentKey, "신청 취소");
            } catch (Exception e) {
                log.error("PG void 실패(수동 정산 필요) paymentKey={}", paymentKey, e);
            }
        }

        // 3. Redis 갱신
        updateApplicantCount(postId, -1L);
    }

    @Transactional(readOnly = true)
    public GetMyPostApplicants getApplicantsByPost(Long postId, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모집글입니다."));
        User currentUser = userDetails.getUser();

        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new AccessDeniedException("본인의 모집글만 조회할 수 있습니다.");
        }

        List<Participation> applicants = participationRepository.findAllByPostIdWithUser(postId);
        List<GetMyPostApplicant> applicantDtos = GetMyPostApplicant.from(applicants);
        return GetMyPostApplicants.from(applicantDtos);
    }

    @Transactional(readOnly = true)
    public List<GetMyPostsParticipationResponseDto> getMyPostsParticipation(Long userId) {
        List<Participation> participations = participationRepository.findByUserIdWithPost(userId);
        List<GetMyPostsParticipationResponseDto> result = new ArrayList<>(participations.size());
        for (Participation p : participations) {
            result.add(convertToDto(p));
        }
        return result;
    }

    private GetMyPostsParticipationResponseDto convertToDto(Participation participation) {
        Post post = participation.getPost();
        int currentPeople = participationRepository.countByPost_IdAndStatus(post.getId(), ApplicationStatus.APPROVED);

        return new GetMyPostsParticipationResponseDto(
                post.getId(),
                post.getTitle(),
                post.getDate().toString(),
                currentPeople,
                post.getMaxPeople(),
                post.getLocation(),
                post.getCost(),
                participation.getStatus(),
                post.getStatus()
        );
    }
}
