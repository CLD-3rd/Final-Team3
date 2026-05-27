package com.matchFit.follow.controller;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.follow.dto.response.FollowApplyResponseDto;
import com.matchFit.follow.dto.response.GetMyFollowResponseDto;
import com.matchFit.follow.service.FollowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/api/posts/{postId}/follow")
    public ResponseEntity<ApiResponseDTO<FollowApplyResponseDto>> toggleFollow(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        FollowApplyResponseDto result = followService.toggleFollow(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.FOLLOW_TOGGLED, result));
    }

    @GetMapping("/api/user/follow")
    public ResponseEntity<ApiResponseDTO<List<GetMyFollowResponseDto>>> getMyFollows(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<GetMyFollowResponseDto> follows = followService.getUserFollows(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_GET_MY_FOLLOWS, follows));
    }
}
