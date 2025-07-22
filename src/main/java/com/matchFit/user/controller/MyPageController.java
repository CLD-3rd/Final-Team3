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
import com.matchFit.user.service.MyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        String email = principal.getUsername(); // UserDetails의 username 필드에 이메일 저장됨
        return ResponseEntity.ok(myPageService.getMyPage(email));
    }

    @PutMapping
    public ResponseEntity<MyPageResponse> editMyPage(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody EditMyPageRequest request
    ) {
        String email = principal.getUsername();
        return ResponseEntity.ok(myPageService.editMyPage(email, request));
    }
}
