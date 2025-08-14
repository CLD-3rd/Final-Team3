package com.matchFit.participation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchFit.participation.dto.request.ManageApplicant;
import com.matchFit.participation.dto.request.ManageApplicant.Decision;
import com.matchFit.participation.dto.response.DecisionApplicant;
import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.dto.response.GetMyPostApplicant;
import com.matchFit.post.dto.response.GetMyPostApplicants;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;
import com.matchFit.post.exception.PostNotFoundException;
import com.matchFit.post.exception.UnauthorizedUserException;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;
import com.matchFit.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipationService {
   
   private final ParticipationRepository participationRepository;
   private final UserRepository userRepository;
   private final PostRepository postRepository;

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
      Post post = postRepository.findById(postId).orElseThrow(
            () -> new PostNotFoundException());

      User currentUser = userDetails.getUser();

      if (!post.getUser().getId().equals(currentUser.getId())) {
         throw new UnauthorizedUserException();
      }

        Participation participation = participationRepository
            .findByPostIdAndUserId(postId, dto.getApplicantId());

//        if (dto.getDecision() == Decision.ACCEPT) {
//            participation.setStatus(ApplicationStatus.APPROVED);
//
//            // 현재 승인된 인원 수 확인
//            int approvedCount = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
//
//            // 승인 완료 처리로 인해 인원이 찼다면 마감 처리
//            if (approvedCount + 1 >= post.getMaxPeople()) {
//                post.setStatus(com.matchFit.post.entity.Status.CLOSED);
//                postRepository.save(post);
//                participationRepository.flush(); 
//            }
//
//        } else {
//            participation.setStatus(ApplicationStatus.REJECTED);
//        }
//      return new DecisionApplicant(
//            participation.getUser().getId(),
//            participation.getUser().getNickname(),
//            participation.getStatus());
      
        if (dto.getDecision() == Decision.ACCEPT) {
          // 이미 승인된 경우 중복 승인 방지
          if (participation.getStatus() == ApplicationStatus.APPROVED) {
              return new DecisionApplicant(
                      participation.getUser().getId(),
                      participation.getUser().getNickname(),
                      participation.getStatus()
              );
          }

          participation.setStatus(ApplicationStatus.APPROVED);
          participationRepository.saveAndFlush(participation); // 변경을 DB에 먼저 반영

          // 승인 인원 재집계 (이번 승인자 포함됨)
          int approvedCount = participationRepository
                  .countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED)+1;

          if (approvedCount >= post.getMaxPeople()) {
              if (post.getStatus() != com.matchFit.post.entity.Status.CLOSED) {
                  post.setStatus(com.matchFit.post.entity.Status.CLOSED);
                  postRepository.save(post);
              }
          }
      } else {
          participation.setStatus(ApplicationStatus.REJECTED);
          participationRepository.save(participation);
      }

      return new DecisionApplicant(
              participation.getUser().getId(),
              participation.getUser().getNickname(),
              participation.getStatus()
      );
        
   }
}
