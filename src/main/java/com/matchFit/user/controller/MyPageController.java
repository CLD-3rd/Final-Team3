package com.matchFit.user.controller;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> getMyPage(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization 헤더가 유효하지 않습니다.");
        }

        String token = authHeader.substring(7); // "Bearer " 제외
        if (!jwtProvider.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 JWT입니다.");
        }

        String email = jwtProvider.getEmailFromToken(token);
        MyPageResponse response = myPageService.getMyPage(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> editMyPage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody EditMyPageRequest request
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization 헤더가 유효하지 않습니다.");
        }

        String token = authHeader.substring(7);
        if (!jwtProvider.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 JWT입니다.");
        }

        String email = jwtProvider.getEmailFromToken(token);
        MyPageResponse response = myPageService.editMyPage(email, request);
        return ResponseEntity.ok(response);
    }
}
