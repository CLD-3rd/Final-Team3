package com.matchFit.follow.controller

import com.matchFit.common.code.SuccessCode
import com.matchFit.common.dto.response.ApiResponseDTO
import com.matchFit.follow.dto.response.FollowApplyResponseDto
import com.matchFit.follow.dto.response.GetMyFollowResponseDto
import com.matchFit.follow.service.FollowService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FollowController(
    private val followService: FollowService
) {
    @PostMapping("/api/posts/{postId}/follow")
    fun toggleFollow(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponseDTO<FollowApplyResponseDto>> {
        val email = userDetails.username
        val result = followService.toggleFollow(email, postId)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.FOLLOW_TOGGLED, result))
    }

    @GetMapping("/api/user/follow")
    fun getMyFollows(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponseDTO<List<GetMyFollowResponseDto>>> {
        val email = userDetails.username
        val follows = followService.getUserFollows(email)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_GET_MY_FOLLOWS, follows))
    }
}
