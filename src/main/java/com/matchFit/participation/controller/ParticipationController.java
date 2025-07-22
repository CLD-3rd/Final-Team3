package com.matchFit.participation.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.participation.dto.response.GetMyPostsParticipationResponseDto;
import com.matchFit.participation.service.ParticipationService;
import com.matchFit.user.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ParticipationController {
    
    private final ParticipationService participationService;
    // JWT 토큰에서 사용자 ID를 추출하는 서비스
    private final JwtProvider jwtTokenProvider; 
    @GetMapping("/apply")
    public ResponseEntity<List<GetMyPostsParticipationResponseDto>> getMyApplications(
            @RequestHeader("Authorization") String token) {
        
        // JWT 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        List<GetMyPostsParticipationResponseDto> myApplications = participationService.GetMyPostsParticipation(userId);
        
        return ResponseEntity.ok(myApplications);
    }
}