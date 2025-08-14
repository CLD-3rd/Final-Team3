package com.matchFit.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchFit.user.dto.request.FindEmailRequest;
import com.matchFit.user.dto.response.FindEmailResponse;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountRecoveryService {
	
	private final UserRepository userRepository;
	
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
	
	private void requireNonBlank(String value, String fieldName) {
	       if (value == null || value.trim().isEmpty()) {
	           throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
	       }
	   }
	
	@Transactional(readOnly = true)
	public FindEmailResponse findEmail(FindEmailRequest req) {
	    requireNonBlank(req.getNickname(), "nickname");
   
	    User user = userRepository.findByNickname(req.getNickname())
	            .orElseThrow(() -> new IllegalArgumentException("일치하는 계정이 없습니다."));
	    return new FindEmailResponse(maskEmail(user.getEmail()));
	}
	
	
	
}
