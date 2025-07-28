package com.matchFit.follow.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.follow.dto.response.FollowApplyResponseDto;
import com.matchFit.follow.dto.response.GetMyFollowResponseDto;
import com.matchFit.follow.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FollowController {
    
    private final FollowService followService;
    
    // 팔로우 토글
    @PostMapping("/api/posts/{postId}/follow")
    public ResponseEntity<ApiResponseDTO<FollowApplyResponseDto>> toggleFollow(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        FollowApplyResponseDto result = followService.toggleFollow(email, postId);
        
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.FOLLOW_TOGGLED, result));
    }
    
    // 내가 팔로우한 모집글 목록 조회
    @GetMapping("/api/user/follow")
    public ResponseEntity<ApiResponseDTO<List<GetMyFollowResponseDto>>> getMyFollows(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        List<GetMyFollowResponseDto> follows = followService.getUserFollows(email);
        
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_GET_MY_FOLLOWS, follows));
    }
}