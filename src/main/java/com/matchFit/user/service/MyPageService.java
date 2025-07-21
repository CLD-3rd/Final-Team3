package com.matchFit.user.service;

import org.springframework.stereotype.Service;

import com.matchFit.user.dto.response.MyPageResponse;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {
	
	private final UserRepository userRepository;
	
    public MyPageResponse getMyPage(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return MyPageResponse.builder()
                .email(user.getEmail())
                .nickName(user.getUsername())
                .town(user.getTown())
                .age(user.getAge())
                .sports(user.getSports())
                .build();
    }

}