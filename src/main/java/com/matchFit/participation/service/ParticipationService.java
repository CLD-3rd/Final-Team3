package com.matchFit.participation.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchFit.participation.dto.request.ManageApplicant;
import com.matchFit.participation.dto.request.ManageApplicant.Decision;
import com.matchFit.participation.dto.response.DecisionApplicant;
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
import com.matchFit.post.exception.UnauthorizedUserException;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.user.exception.UserNotFoundException;
import com.matchFit.user.repository.UserRepository;
import com.matchFit.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationService {
   
   private final ParticipationRepository participationRepository;
   private final UserRepository userRepository;
   private final PostRepository postRepository;
   private final StringRedisTemplate redisTemplate;
   
   private static final String APPLICANT_KEY_FMT = "applicants:post_%d";
   private static final Integer APPLICANT_KEY_TTL_MINIUTES = 5;

   private String getApplicantKey(Long postId) {
       return String.format(APPLICANT_KEY_FMT, postId);
   }
   
   // 모집 글 신청
   @Transactional
   public void applyPost(Long postId, Long userId) {
      User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
            
      Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("모집 글이 존재하지 않습니다"));
      
      // 이미 신청한 경우
      if (participationRepository.findByPostIdAndUserId(postId, userId) != null) {
         throw new IllegalStateException("이미 신청한 모집글입니다");
      }
      // 마감 체크
      int currentPeople = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
      
       if (currentPeople >= post.getMaxPeople()) {
           throw new IllegalStateException("마감되었습니다");
       }
      
      Participation participation = new Participation(user, post);
      participationRepository.saveAndFlush(participation);
      
      
   }

   // 모집 글 신청 취소
   @Transactional
   public void cancelApplyPost(Long postId, Long userId) {
       User user = userRepository.findById(userId)
               .orElseThrow(() -> new UserNotFoundException());
               
       Post post = postRepository.findById(postId)
               .orElseThrow(() -> new PostNotFoundException());
       
       Participation participation = participationRepository.findByPostIdAndUserId(postId, userId);
       
       // 경기 하루 전부터는 취소 불가
       LocalDateTime now = LocalDateTime.now();
       LocalDateTime eventTime = post.getDate();

       // 경기 전날 00:00부터 제한
       LocalDateTime cutoffTime = eventTime.toLocalDate().minusDays(1).atStartOfDay();

       if (now.isAfter(cutoffTime)) {
           throw new ParticipationCancellationTimeExceededException();
       }
       
       boolean wasApproved = participation.getStatus() == ApplicationStatus.APPROVED;
       
       // 신청 삭제
       participationRepository.delete(participation);
       
       // 승인된 참여자가 취소한 경우 post 상태 업데이트
       if (wasApproved) {
           int currentApproved = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
           
           // 취소로 인해 자리가 생긴 경우 모집 재개
           if (currentApproved < post.getMaxPeople() && post.getStatus() == Status.CLOSED) {
               post.setStatus(Status.OPEN);
               postRepository.save(post);
           }
       }
   }
   
   
   // 신청자 목록 조회
   public GetMyPostApplicants getApplicantsByPost(Long postId, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모집글입니다."));
        User currentUser = userDetails.getUser();
        
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인의 모집글만 조회할 수 있습니다.");
        }
        
        List<Participation> applicants = participationRepository.findAllByPostId(postId);
        List<GetMyPostApplicant> applicantDtos = GetMyPostApplicant.from(applicants);
        
        return GetMyPostApplicants.of(applicantDtos);
    }
   
   
   // 내가 신청한 글 목록 조회
    public List<GetMyPostsParticipationResponseDto> GetMyPostsParticipation(Long userId) {
        List<Participation> participations = participationRepository.findByUserIdWithPost(userId);
        
        return participations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private GetMyPostsParticipationResponseDto convertToDto(Participation participation) {
        ApplicationStatus applicationStatus = participation.getStatus();
        
        Post post = participation.getPost();
        Long postId = post.getId();           
        String title = post.getTitle();
        LocalDateTime date = post.getDate();
        Integer maxPeople = post.getMaxPeople();
        String location = post.getLocation(); 
        Integer cost = post.getCost();     
        Status postStatus = post.getStatus();
        
        int currentPeople = participationRepository.countByPost_IdAndStatus(
                post.getId(), 
                ApplicationStatus.APPROVED
        )+1;
        
        return new GetMyPostsParticipationResponseDto(
                postId,                         
                title,                              
                date.toString(),                    
                currentPeople,                      
                maxPeople,
                location,                       
                cost,                        
                applicationStatus,
                postStatus
        );
    }

    @Transactional
    public DecisionApplicant manageApplicant(Long postId, ManageApplicant dto, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        User currentUser = userDetails.getUser();
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedUserException();
        }

        Participation participation = participationRepository
                .findByPostIdAndUserId(postId, dto.getApplicantId());

        ApplicationStatus previous = participation.getStatus();

        if (dto.getDecision() == Decision.ACCEPT) {
            // 이미 승인된 경우 중복 승인 방지
            if (previous == ApplicationStatus.APPROVED) {
                return new DecisionApplicant(
                        participation.getUser().getId(),
                        participation.getUser().getNickname(),
                        participation.getStatus()
                );
            }

            // DB에 승인 반영
            participation.setStatus(ApplicationStatus.APPROVED);
            participationRepository.saveAndFlush(participation);

            // Redis: 해당 포스트 키를 INCR (key가 없으면 안전하게 DB 기준으로 초기화)
            String key = getApplicantKey(postId);
            try {
                Long newCount = redisTemplate.opsForValue().increment(key, 1);
                if (newCount == null) {
                    // Redis가 비어 있거나 이상한 경우 DB에서 집계하여 초기화 (DB count는 APPROVED 수 -> 작성자 포함하려면 +1)
                    long approvedCount = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
                    long totalIncludingAuthor = approvedCount + 1; // 작성자 포함
                    redisTemplate.opsForValue().set(key, String.valueOf(totalIncludingAuthor), Duration.ofMinutes(APPLICANT_KEY_TTL_MINIUTES));
                    newCount = totalIncludingAuthor;
                }

                // 모집 마감 체크 (Redis 기준)
                if (newCount >= post.getMaxPeople() && post.getStatus() != Status.CLOSED) {
                    post.setStatus(Status.CLOSED);
                    postRepository.save(post);
                }
            } catch (Exception e) {
                // Redis 실패는 DB 반영에 영향주지 않음 — 로그만 남기고 진행
               log.error("Redis update failed for post {}: {}", postId, e.getMessage(), e);
            }

        } else { // REJECT: DB만 업데이트, Redis는 건드리지 않음
            participation.setStatus(ApplicationStatus.REJECTED);
            participationRepository.saveAndFlush(participation);

            // 주의: 이전에 APPROVED였더라도 Redis에서 DECR 하지 않음(요청하신 대로)
        }

        return new DecisionApplicant(
                participation.getUser().getId(),
                participation.getUser().getNickname(),
                participation.getStatus()
        );
    }
}
