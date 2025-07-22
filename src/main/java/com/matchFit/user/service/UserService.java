package com.matchFit.user.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
	public Long findUserIdByEmail(String email) {
	    return userRepository.findByEmail(email)
	        .map(User::getId)
	        .orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않음"));
	}

}
