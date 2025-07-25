package com.matchFit.participation.dto.response;

import com.matchFit.participation.entity.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DecisionApplicant {
	Long applicantId;
	String nickName;
	ApplicationStatus decision;
}
