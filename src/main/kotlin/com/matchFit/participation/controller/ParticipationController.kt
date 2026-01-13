package com.matchFit.participation.controller

import com.matchFit.common.code.SuccessCode
import com.matchFit.common.dto.response.ApiResponseDTO
import com.matchFit.participation.dto.request.ManageApplicant
import com.matchFit.participation.dto.response.DecisionApplicant
import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto
import com.matchFit.participation.service.ParticipationService
import com.matchFit.user.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/posts")
class ParticipationController(
    private val participationService: ParticipationService
) {
    @GetMapping("/apply")
    fun getMyApplications(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<List<GetMyPostsParticipationResponseDto>>> {
        val userId = userDetails.user.id!!
        val myApplications = participationService.getMyPostsParticipation(userId)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_APPLIED_POSTS, myApplications))
    }

    @PatchMapping("/{postId}/apply")
    fun manageApplicant(
        @PathVariable postId: Long,
        @RequestBody dto: ManageApplicant,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<DecisionApplicant>> {
        val response = participationService.manageApplicant(postId, dto, userDetails)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.PARTICIPATION_MANAGED, response))
    }

    @DeleteMapping("/{postId}/apply")
    fun cancelApplyPost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<Any>> {
        val userId = userDetails.user.id!!
        participationService.cancelApplyPost(postId, userId)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_CANCEL_APPLY, null))
    }
}
