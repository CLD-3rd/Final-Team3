package com.matchFit.user.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.matchFit.user.entity.User;
import com.matchFit.user.jwt.JwtProvider;
import com.matchFit.user.repository.UserRepository;

@Controller 
public class KakaoOAuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtProvider jwtProvider;
    
    @Value("${kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${kakao.redirect-uri.signup}")
    private String kakaoSignupRedirectUri;
    
    @Value("${kakao.redirect-uri.login}")
    private String kakaoLoginRedirectUri;
    

    
    // 카카오 설정 API
    @GetMapping("/api/kakao-config")
    @ResponseBody
    public Map<String, String> getKakaoConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("clientId", kakaoClientId);
        config.put("signupRedirectUri", kakaoSignupRedirectUri);
        config.put("loginRedirectUri", kakaoLoginRedirectUri);
        return config;
    }
    
    // 카카오 회원가입용 콜백
    @GetMapping("/api/user/oauth/kakao/callback")
    public String kakaoSignupCallback(@RequestParam String code) {
        try {

            String email = getKakaoEmail(code, kakaoSignupRedirectUri);
            System.out.println("=== 카카오 회원가입 콜백 성공! 이메일: " + email + " ===");
            // 수정한 부분 -> 이래야 프론트 회원가입 페이지로 리다이렉트됨.
            return "redirect:http://localhost:3000/signup?kakaoEmail=" + email;
        } catch (Exception e) {
            System.out.println("=== 카카오 회원가입 콜백 실패: " + e.getMessage() + " ===");
            e.printStackTrace();
            return "redirect:http://localhost:3000/signup?error=kakao_error";
        }
    }
    
    // 카카오 로그인용 콜백
    @GetMapping("/api/user/oauth/kakao/login-callback")
    public String kakaoLoginCallback(@RequestParam String code) {
        try {

            String email = getKakaoEmail(code, kakaoLoginRedirectUri);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                // 기존 사용자 - JWT 토큰 생성하여 로그인 페이지로 리다이렉트
                String jwt = jwtProvider.createToken(user.getId(), user.getEmail());
                return "redirect:http://localhost:3000/login?kakaoToken=" + jwt;
            } else {
                // 신규 사용자 - 회원가입 페이지로 리다이렉트
                System.out.println("=== 신규 사용자, 회원가입으로 이동: " + email + " ===");
                return "redirect:http://localhost:3000/signup?kakaoEmail=" + email;
            }
        } catch (Exception e) {
            System.out.println("=== 카카오 로그인 콜백 실패: " + e.getMessage() + " ===");
            return "redirect:http://localhost:3000/login?error=kakao_error";
        }
    }
    
    private String getKakaoEmail(String code, String redirectUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // 액세스 토큰 받기
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoClientId);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);
            
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", tokenRequest, Map.class);
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            System.out.println("=== 카카오 액세스 토큰 받기 성공 ===");
            
            // 사용자 정보 받기
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", 
                HttpMethod.GET, userRequest, Map.class);
            
            Map<String, Object> kakaoAccount = (LinkedHashMap<String, Object>) userResponse.getBody().get("kakao_account");
            String email = (String) kakaoAccount.get("email");
            
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("카카오에서 이메일 정보를 가져올 수 없습니다");
            }
            
            System.out.println("=== 카카오 사용자 정보 받기 성공: " + email + " ===");
            return email;
            
        } catch (Exception e) {
            System.out.println("=== 카카오 API 호출 실패: " + e.getMessage() + " ===");
            throw new RuntimeException("카카오 로그인 실패: " + e.getMessage());
        }
    }
}