package com.matchFit.participation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.participation.dto.request.ManageApplicant;
import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.service.ParticipationService;
import com.matchFit.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ParticipationController {
    
    private final ParticipationService participationService;
    
    @GetMapping("/apply")
    public ResponseEntity<ApiResponseDTO<List<GetMyPostsParticipationResponseDto>>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // CustomUserDetails에서 userId 가져오기
        Long userId = userDetails.getUser().getId();  // User 엔티티에서 ID 추출
        
        List<GetMyPostsParticipationResponseDto> myApplications = participationService.GetMyPostsParticipation(userId);
        
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_APPLIED_POSTS, myApplications));
    }
    
    @PatchMapping("/{postId}/apply")
	public ResponseEntity<ApiResponseDTO<Void>> manageApplicant(@PathVariable Long postId,
			@RequestBody ManageApplicant dto, 
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		participationService.manageApplicant(postId, dto, userDetails);
		return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.PARTICIPATION_MANAGED, null));
	}
}