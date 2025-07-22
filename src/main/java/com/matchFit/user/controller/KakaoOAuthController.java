package com.matchFit.user.controller;


import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller 
public class KakaoOAuthController {

    @Autowired
    private UserRepository userRepository;

    // 카카오 회원가입용 콜백
    @GetMapping("/api/user/oauth/kakao/callback")
    public String kakaoSignupCallback(@RequestParam String code) {
        try {
            String email = getKakaoEmail(code, "http://localhost:8083/api/user/oauth/kakao/callback");
            System.out.println("=== 카카오 회원가입 콜백 성공! 이메일: " + email + " ===");
            return "redirect:/signup?kakaoEmail=" + email;
        } catch (Exception e) {
            System.out.println("=== 카카오 회원가입 콜백 실패: " + e.getMessage() + " ===");
            e.printStackTrace();
            return "redirect:/signup?error=kakao_error";
        }
    }
    
    // 카카오 로그인용 콜백
    @GetMapping("/api/user/oauth/kakao/login-callback")
    public String kakaoLoginCallback(@RequestParam String code, HttpSession session) {
        try {
            String email = getKakaoEmail(code, "http://localhost:8083/api/user/oauth/kakao/login-callback");
            
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user != null) {
                session.setAttribute("userEmail", user.getEmail());
                System.out.println("=== 카카오 로그인 성공: " + email + " ===");
                return "redirect:/dashboard";
            } else {
                System.out.println("=== 신규 사용자, 회원가입으로 이동: " + email + " ===");
                return "redirect:/signup?kakaoEmail=" + email;
            }
            
        } catch (Exception e) {
            System.out.println("=== 카카오 로그인 콜백 실패: " + e.getMessage() + " ===");
            return "redirect:/login?error=true";
        }
    }
    
    private String getKakaoEmail(String code, String redirectUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // 토큰 받기
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", "ae217452e8bbfd6450ca5024a003504e");
            params.add("redirect_uri", redirectUri);
            params.add("code", code);
            
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", tokenRequest, Map.class);
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            System.out.println("=== 카카오 토큰 받기 성공 ===");
            
            // 사용자 정보 받기
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", 
                HttpMethod.GET, userRequest, Map.class);
            
            Map<String, Object> kakaoAccount = (LinkedHashMap<String, Object>) userResponse.getBody().get("kakao_account");
            String email = (String) kakaoAccount.get("email");
            
            System.out.println("=== 카카오 사용자 정보 받기 성공: " + email + " ===");
            return email;
            
        } catch (Exception e) {
            System.out.println("=== 카카오 API 호출 실패: " + e.getMessage() + " ===");
            throw new RuntimeException("카카오 로그인 실패: " + e.getMessage());
        }
    }
}