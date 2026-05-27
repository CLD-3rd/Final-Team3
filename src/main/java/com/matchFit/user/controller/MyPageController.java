package com.matchFit.user.controller;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.user.dto.request.EditMyPageRequest;
import com.matchFit.user.dto.response.MyPageResponse;
import com.matchFit.user.security.CustomUserDetails;
import com.matchFit.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyPageResponse response = myPageService.getMyPage(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_GET_MY_PROFILE, response));
    }

    @PutMapping
    public ResponseEntity<ApiResponseDTO<MyPageResponse>> editMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody EditMyPageRequest request
    ) {
        String email = userDetails.getUsername();
        MyPageResponse response = myPageService.editMyPage(email, request);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_EDIT_MY_PROFILE, response));
    }
}
