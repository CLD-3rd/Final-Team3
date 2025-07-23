package com.matchFit.participation.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.dto.response.GetMyPostApplicant;
import com.matchFit.post.dto.response.GetMyPostApplicants;
import com.matchFit.post.entity.Post;
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
	public void applyPost(Long postId, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
				
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("모집 글이 존재하지 않습니다"));
		
		// 마감 체크
		int currentPeople = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
		
	    if (currentPeople >= post.getMaxPeople()) {
	        throw new IllegalStateException("마감되었습니다");
	    }
		
		Participation participation = new Participation(user, post);
		participationRepository.save(participation);
		
		
	}

	
	public GetMyPostApplicants getApplicantsByPost(Long postId, @AuthenticationPrincipal CustomUserDetails userDetails) {
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
}
