package com.matchFit.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetConfirmRequest {
	
	private String email;
	private String token;
	private String newPassword;
}
