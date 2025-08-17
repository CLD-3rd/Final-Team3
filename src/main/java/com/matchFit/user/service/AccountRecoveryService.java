package com.matchFit.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.exception.GeneralException;
import com.matchFit.user.dto.request.FindEmailRequest;
import com.matchFit.user.dto.request.PasswordResetRequest;
import com.matchFit.user.dto.response.FindEmailResponse;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;
import com.matchFit.user.token.RedisPasswordResetToken;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountRecoveryService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RedisPasswordResetToken tokenStore;
	
	private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend.reset-password-url}")
    private String resetUrlBase;

    private void requireNonBlank(String v, String name) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(name + "은(는) 필수입니다.");
    }
	
    // 비밀번호 재설정 링크 요청: req.email 필요 
    public void requestReset(PasswordResetRequest req) {
        requireNonBlank(req.getEmail(), "email");

        Optional<User> opt = userRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
          	throw new GeneralException(ErrorCode.EMAIL_NOT_FOUND);
        }       
        
        User user = opt.get();
        String token = tokenStore.issueToken(user.getId());
        sendPasswordResetMail(user.getEmail(), token);
    }

    // 비밀번호 재설정 확정: req.token + req.newPassword 필요 
    public void confirmReset(PasswordResetRequest req) {
        requireNonBlank(req.getToken(), "token");
        requireNonBlank(req.getNewPassword(), "newPassword");
        if (req.getNewPassword().length() < 8)
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");

        Long userId = tokenStore.peekUserId(req.getToken());
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        
        // 새 비밀번호가 기존 비밀번호와 일치하는지 판단
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new GeneralException(ErrorCode.PASSWORD_SAME);
        }

        
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        //  여기서만 토큰 소모(또는 used=true로 마킹)
        tokenStore.consumeToken(req.getToken());
    }

    //  내부 메일 발송 유틸 
    private void sendPasswordResetMail(String to, String token) {
        String link = resetUrlBase + token;
        String subject = "[MatchFit] 비밀번호 재설정 안내";
        String body = """
            안녕하세요.

            비밀번호 재설정을 요청하셨다면 아래 링크를 클릭하세요.
        

            %s

   
            """.formatted(link);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);  
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
    
	
	// 이메일 찾기
    @Transactional(readOnly = true)
	public FindEmailResponse findEmail(FindEmailRequest req) {
	    requireNonBlank(req.getNickname(), "nickname");
   
	    User user = userRepository.findByNickname(req.getNickname())
	            .orElseThrow(() -> new IllegalArgumentException("일치하는 계정이 없습니다."));
	    return new FindEmailResponse(maskEmail(user.getEmail()));
	}
    
	private String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        String local = email.substring(0, at);
        String domain = email.substring(at);
        int keep = Math.max(1, local.length() / 3);
        String visible = local.substring(0, keep);
        String stars = "*".repeat(Math.max(1, local.length() - keep));
        return visible + stars + domain;
    }
		
}
