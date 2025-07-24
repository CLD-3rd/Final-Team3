package com.matchFit.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.user.dto.request.EditMyPageRequest;
import com.matchFit.user.dto.response.MyPageResponse;
import com.matchFit.user.security.CustomUserDetails;
import com.matchFit.user.service.MyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername(); 
        MyPageResponse response = myPageService.getMyPage(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<MyPageResponse> editMyPage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody EditMyPageRequest request) {
        
        String email = userDetails.getUsername();
        MyPageResponse response = myPageService.editMyPage(email, request);
        return ResponseEntity.ok(response);  
    }

}