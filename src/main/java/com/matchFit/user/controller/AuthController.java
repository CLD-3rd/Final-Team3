package com.matchFit.user.controller;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.post.entity.Sports;
import com.matchFit.user.dto.request.FindEmailRequest;
import com.matchFit.user.dto.request.PasswordResetRequest;
import com.matchFit.user.dto.request.SignUpRequest;
import com.matchFit.user.dto.response.FindEmailResponse;
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
import com.matchFit.user.service.AccountRecoveryService;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {

    private final AccountRecoveryService accountRecoveryService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @GetMapping("/signup")
    public ModelAndView signupPage() {
        return new ModelAndView("forward:/signup.html");
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDTO<Void>> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistException();
        }

        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            throw new NicknameAlreadyExistException();
        }

        Gender gender;
        try {
            gender = Gender.valueOf(signUpRequest.getGender());
        } catch (IllegalArgumentException ex) {
            throw new GenderInvalidException();
        }

        Sports sports;
        try {
            sports = Sports.valueOf(signUpRequest.getSports());
        } catch (IllegalArgumentException ex) {
            throw new SportsInvalidException();
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setNickname(signUpRequest.getNickname());
        user.setGender(gender);
        user.setSports(sports);
        user.setAge(signUpRequest.getAge());
        user.setTown(signUpRequest.getTown());

        String rawPassword = signUpRequest.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            user.setPassword("KAKAO_LOGIN");
        } else {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_CREATED, null));
    }

    @PostMapping("/check-email")
    public ResponseEntity<ApiResponseDTO<Void>> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email != null && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistException();
        }
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_EMAIL_AVAILABLE, null));
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<ApiResponseDTO<Void>> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        if (nickname != null && userRepository.existsByNickname(nickname)) {
            throw new NicknameAlreadyExistException();
        }
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_NICKNAME_AVAILABLE, null));
    }

    @GetMapping("/login")
    public ModelAndView loginPage() {
        return new ModelAndView("forward:/login.html");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> loginUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userRepository.findByEmail(email == null ? "" : email)
                .orElseThrow(UserNotFoundException::new);

        if ("KAKAO_LOGIN".equals(user.getPassword())) {
            throw new KakaoLoginException();
        }

        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String jwt = jwtProvider.createToken(user.getId(), user.getEmail());
        Map<String, String> payload = Collections.singletonMap("token", jwt);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, payload));
    }

    @PostMapping("/find-email")
    public ResponseEntity<ApiResponseDTO<FindEmailResponse>> findEmail(@RequestBody FindEmailRequest findEmailRequest) {
        FindEmailResponse response = accountRecoveryService.findEmail(findEmailRequest);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_FIND_MY_EMAIL, response));
    }

    @PostMapping("/request-password")
    public ResponseEntity<ApiResponseDTO<Void>> requestPasswordReset(@RequestBody PasswordResetRequest req) {
        accountRecoveryService.requestReset(req);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_REQUEST_PASSWORD_RESET, null));
    }

    @PostMapping("/confirm-password")
    public ResponseEntity<ApiResponseDTO<Void>> confirmPasswordReset(@RequestBody PasswordResetRequest req) {
        accountRecoveryService.confirmReset(req);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_CONFIRMED_PASSWORD_RESET, null));
    }
}
