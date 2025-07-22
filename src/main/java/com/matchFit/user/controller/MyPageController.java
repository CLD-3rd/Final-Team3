package com.matchFit.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.user.dto.request.EditMyPageRequest;
import com.matchFit.user.dto.response.MyPageResponse;
import com.matchFit.user.jwt.JwtProvider;
import com.matchFit.user.service.MyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final JwtProvider jwtProvider;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@RequestHeader("Authorization") String authHeader) {
        String email = jwtProvider.getEmailFromToken(authHeader.substring(7));
        return ResponseEntity.ok(myPageService.getMyPage(email));
    }

    @PutMapping
    public ResponseEntity<MyPageResponse> editMyPage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody EditMyPageRequest request
    ) {
        String email = jwtProvider.getEmailFromToken(authHeader.substring(7));
        return ResponseEntity.ok(myPageService.editMyPage(email, request));
    }
    
}
