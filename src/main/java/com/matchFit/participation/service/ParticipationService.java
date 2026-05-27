package com.matchFit.participation.service;

import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.exception.ParticipationCancellationTimeExceededException;
import com.matchFit.participation.repository.ParticipationRepository;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationService {

    private static final Logger log = LoggerFactory.getLogger(ParticipationService.class);

    private static final String APPLICANT_KEY_FMT = "applicants:post_%d";
    private static final int APPLICANT_KEY_TTL_MINUTES = 5;

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final StringRedisTemplate redisTemplate;

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

    public void applyPost(Long postId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if (participationRepository.findByPostIdAndUserId(postId, userId) != null) {
            throw new IllegalStateException("이미 신청한 모집글입니다");
        }

        int currentPeople = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
        if (currentPeople >= post.getMaxPeople()) {
            throw new IllegalStateException("마감되었습니다");
        }

        participationRepository.saveAndFlush(new Participation(user, post));

        Long newCountBoxed = updateApplicantCount(postId, 1L);
        long newCount = newCountBoxed == null ? 0L : newCountBoxed;
        if (newCount >= (long) post.getMaxPeople() && post.getStatus() != Status.CLOSED) {
            post.setStatus(Status.CLOSED);
            postRepository.save(post);
        }
    }

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

        participationRepository.delete(participation);
        updateApplicantCount(postId, -1L);

        int currentApproved = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
        if (currentApproved < post.getMaxPeople() && post.getStatus() == Status.CLOSED) {
            post.setStatus(Status.OPEN);
            postRepository.save(post);
        }
    }

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
