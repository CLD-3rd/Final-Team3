package com.matchFit.user.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.post.entity.Sports;
import com.matchFit.user.dto.request.SignUpRequest;
import com.matchFit.user.entity.Gender;
import com.matchFit.user.entity.User;
import com.matchFit.user.exception.EmailAlreadyExistException;
import com.matchFit.user.exception.GenderInvalidException;
import com.matchFit.user.exception.InvalidPasswordException;
import com.matchFit.user.exception.KakaoLoginException;
import com.matchFit.user.exception.NicknameAlreadyExistException;
import com.matchFit.user.exception.SportsInvalidException;
import com.matchFit.user.exception.UserNotFoundException;
import com.matchFit.user.jwt.JwtProvider;
import com.matchFit.user.repository.UserRepository;

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
        	throw new EmailAlreadyExistException();
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            throw new NicknameAlreadyExistException();
        }
        
        Gender gender;
        try {
            gender = Gender.valueOf(signUpRequest.getGender());
        } catch (IllegalArgumentException e) {
            throw new GenderInvalidException();
        }

        Sports sports;
        try {
            sports = Sports.valueOf(signUpRequest.getSports());
        } catch (IllegalArgumentException e) {
            throw new SportsInvalidException();
        }

        // 사용자 생성
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setNickname(signUpRequest.getNickname());
        user.setGender(gender);
        user.setSports(sports);
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

    }

    

    // 이메일 중복 확인
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistException();
        }
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_EMAIL_AVAILABLE, null));
    }

    // 닉네임 중복 확인
    @PostMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        if (userRepository.existsByNickname(nickname)) {
            throw new NicknameAlreadyExistException();
        }
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_NICKNAME_AVAILABLE, null));
    }
    
    // 로그인 - GET 요청 (HTML 페이지 반환)
    @GetMapping("/login")
    public ModelAndView loginPage() {
        return new ModelAndView("forward:/login.html");
    }

    @Autowired
    private JwtProvider jwtProvider;
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            throw new UserNotFoundException();
        }

        // 카카오 회원이면 일반 로그인 차단
        if ("KAKAO_LOGIN".equals(user.getPassword())) {
            throw new KakaoLoginException();
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String jwt = jwtProvider.createToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, Collections.singletonMap("token", jwt)));
        
    }
    
}