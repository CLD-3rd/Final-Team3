package com.matchFit.participation.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.dto.response.GetMyPostApplicant;
import com.matchFit.post.dto.response.GetMyPostApplicants;
import com.matchFit.post.entity.Post;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipationService {
	private final ParticipationRepository participationRepository;
	private final PostRepository postRepository;
	
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
