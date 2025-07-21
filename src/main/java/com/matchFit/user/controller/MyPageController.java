package com.matchFit.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.user.dto.response.MyPageResponse;
import com.matchFit.user.service.MyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
public class MyPageController {
	
    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@RequestHeader("accessToken") String token) {
        String email = extractEmailFromToken(token);
        return ResponseEntity.ok(myPageService.getMyPage(email));
    }

	private String extractEmailFromToken(String token) {
		return token.replace("Bearer ", "");
	}
    
}
