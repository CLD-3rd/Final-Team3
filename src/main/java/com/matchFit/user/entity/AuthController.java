package com.matchFit.user.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.post.entity.Sports;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            return ResponseEntity.badRequest().body("이미 사용중인 닉네임입니다.");
        }

        try {
            // 새 사용자 생성 (String을 enum으로 변환)
            User user = new User();
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setNickname(signUpRequest.getNickname());

            user.setGender(Gender.valueOf(signUpRequest.getGender()));
            user.setSports(Sports.valueOf(signUpRequest.getSports()));
            user.setAge(signUpRequest.getAge());
            user.setTown(signUpRequest.getTown());

            userRepository.save(user);
            return ResponseEntity.ok("회원가입이 완료되었습니다!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 성별 또는 스포츠 종목입니다.");
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
    
    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("userEmail") == null) {
            return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
        }
        
        String currentEmail = (String) session.getAttribute("userEmail");
        User user = userRepository.findByEmail(currentEmail).orElse(null);
        
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
    
    
}
