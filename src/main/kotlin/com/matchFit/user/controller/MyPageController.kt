package com.matchFit.user.controller

import com.matchFit.common.code.SuccessCode
import com.matchFit.common.dto.response.ApiResponseDTO
import com.matchFit.user.dto.request.EditMyPageRequest
import com.matchFit.user.dto.response.MyPageResponse
import com.matchFit.user.security.CustomUserDetails
import com.matchFit.user.service.MyPageService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/user/mypage")
class MyPageController(
    private val myPageService: MyPageService
) {
    @GetMapping
    fun getMyPage(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<ApiResponseDTO<MyPageResponse>> {
        val email = userDetails.username
        val response = myPageService.getMyPage(email)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_GET_MY_PROFILE, response))
    }

    @PutMapping
    fun editMyPage(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: EditMyPageRequest
    ): ResponseEntity<ApiResponseDTO<MyPageResponse>> {
        val email = userDetails.username
        val response = myPageService.editMyPage(email, request)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_EDIT_MY_PROFILE, response))
    }
}
