package com.matchFit.participation.controller;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.service.ParticipationService;
import com.matchFit.user.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @GetMapping("/apply")
    public ResponseEntity<ApiResponseDTO<List<GetMyPostsParticipationResponseDto>>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<GetMyPostsParticipationResponseDto> myApplications = participationService.getMyPostsParticipation(userId);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_APPLIED_POSTS, myApplications));
    }

    @DeleteMapping("/{postId}/apply")
    public ResponseEntity<ApiResponseDTO<Object>> cancelApplyPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        participationService.cancelApplyPost(postId, userId);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_CANCEL_APPLY, null));
    }
}
