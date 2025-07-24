package com.matchFit.user.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.post.entity.Sports;
import com.matchFit.user.dto.request.SignUpRequest;
import com.matchFit.user.entity.Gender;
import com.matchFit.user.entity.User;
import com.matchFit.user.jwt.JwtProvider;
import com.matchFit.user.repository.UserRepository;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 회원가입 - GET 요청 (HTML 페이지 반환)
    @GetMapping("/signup")
    public ModelAndView signupPage() {
        return new ModelAndView("forward:/signup.html");
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDTO<Void>> registerUser(@RequestBody SignUpRequest signUpRequest) {

        // 이메일 중복 확인
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponseDTO.onFailure("USER400", "이미 사용중인 이메일입니다.", null));
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponseDTO.onFailure("USER401", "이미 사용중인 닉네임입니다.", null));
        }

        try {
            // 사용자 생성
            User user = new User();
            user.setEmail(signUpRequest.getEmail());
            user.setNickname(signUpRequest.getNickname());
            user.setGender(Gender.valueOf(signUpRequest.getGender()));
            user.setSports(Sports.valueOf(signUpRequest.getSports()));
            user.setAge(signUpRequest.getAge());
            user.setTown(signUpRequest.getTown());

            // 비밀번호 설정
            if (signUpRequest.getPassword() == null || signUpRequest.getPassword().isEmpty()) {
                user.setPassword("KAKAO_LOGIN");
            } else {
                user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            }

            userRepository.save(user);

            return ResponseEntity
                    .ok(ApiResponseDTO.onSuccess(SuccessCode.USER_CREATED, null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponseDTO.onFailure("USER402", "잘못된 성별 또는 스포츠 종목입니다.", null));
        }
    }

    

    // 이메일 중복 확인
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    // 닉네임 중복 확인
    @PostMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        if (userRepository.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body("이미 사용중인 닉네임입니다.");
        }
        return ResponseEntity.ok("사용 가능한 닉네임입니다.");
    }
    
    // 로그인 - GET 요청 (HTML 페이지 반환)
    @GetMapping("/login")
    public ModelAndView loginPage() {
        return new ModelAndView("forward:/login.html");
    }
    
    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("authenticated", true);
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("age", user.getAge());
            userInfo.put("town", user.getTown());
            userInfo.put("gender", user.getGender().name());
            userInfo.put("sports", user.getSports().name());
            return ResponseEntity.ok(userInfo);
        }
        
        return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
    }

    @Autowired
    private JwtProvider jwtProvider;
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("존재하지 않는 사용자입니다.");
        }

        // 카카오 회원이면 일반 로그인 차단
        if ("KAKAO_LOGIN".equals(user.getPassword())) {
            return ResponseEntity.status(403).body("카카오 회원은 일반 로그인할 수 없습니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String jwt = jwtProvider.createToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(Collections.singletonMap("token", jwt));
        
    }
    
    
    

    
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("토큰 없음");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtProvider.validateToken(token);

        String email = claims.get("email", String.class);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("사용자 없음");

        return ResponseEntity.ok(user);
    }

    
}