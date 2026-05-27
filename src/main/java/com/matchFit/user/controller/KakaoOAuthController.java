package com.matchFit.user.controller;

import com.matchFit.user.entity.User;
import com.matchFit.user.jwt.JwtProvider;
import com.matchFit.user.repository.UserRepository;
import java.util.Map;
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

@Controller
public class KakaoOAuthController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final String kakaoClientId;
    private final String kakaoSignupRedirectUri;
    private final String kakaoLoginRedirectUri;

    public KakaoOAuthController(
            UserRepository userRepository,
            JwtProvider jwtProvider,
            @Value("${kakao.client-id}") String kakaoClientId,
            @Value("${kakao.redirect-uri.signup}") String kakaoSignupRedirectUri,
            @Value("${kakao.redirect-uri.login}") String kakaoLoginRedirectUri
    ) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.kakaoClientId = kakaoClientId;
        this.kakaoSignupRedirectUri = kakaoSignupRedirectUri;
        this.kakaoLoginRedirectUri = kakaoLoginRedirectUri;
    }

    @GetMapping("/api/kakao-config")
    @ResponseBody
    public Map<String, String> getKakaoConfig() {
        return Map.of(
                "clientId", kakaoClientId,
                "signupRedirectUri", kakaoSignupRedirectUri,
                "loginRedirectUri", kakaoLoginRedirectUri
        );
    }

    @GetMapping("/api/user/oauth/kakao/callback")
    public String kakaoSignupCallback(@RequestParam String code) {
        try {
            String email = getKakaoEmail(code, kakaoSignupRedirectUri);
            return "redirect:https://www.match-fit.store/signup/index.html?kakaoEmail=" + email;
        } catch (Exception ex) {
            return "redirect:http://localhost:3000/signup?error=kakao_error";
        }
    }

    @GetMapping("/api/user/oauth/kakao/login-callback")
    public String kakaoLoginCallback(@RequestParam String code) {
        try {
            String email = getKakaoEmail(code, kakaoLoginRedirectUri);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                String jwt = jwtProvider.createToken(user.getId(), user.getEmail());
                return "redirect:https://www.match-fit.store/login/index.html?kakaoToken=" + jwt;
            } else {
                return "redirect:https://www.match-fit.store/signup/index.html?kakaoEmail=" + email;
            }
        } catch (Exception ex) {
            return "redirect:http://localhost:3000/login?error=kakao_error";
        }
    }

    private String getKakaoEmail(String code, String redirectUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoClientId);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token",
                    tokenRequest,
                    Map.class
            );

            Object accessTokenObj = tokenResponse.getBody() == null ? null : tokenResponse.getBody().get("access_token");
            if (!(accessTokenObj instanceof String accessToken)) {
                throw new RuntimeException("카카오 액세스 토큰이 없습니다");
            }

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userRequest,
                    Map.class
            );

            Object kakaoAccountObj = userResponse.getBody() == null ? null : userResponse.getBody().get("kakao_account");
            if (!(kakaoAccountObj instanceof Map<?, ?> kakaoAccount)) {
                throw new RuntimeException("카카오 계정 정보가 없습니다");
            }
            Object emailObj = kakaoAccount.get("email");
            if (!(emailObj instanceof String email)) {
                throw new RuntimeException("카카오에서 이메일 정보를 가져올 수 없습니다");
            }

            return email;
        } catch (Exception ex) {
            throw new RuntimeException("카카오 로그인 실패: " + ex.getMessage());
        }
    }
}
