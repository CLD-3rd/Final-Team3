package com.matchFit.participation.entity;

public enum ApplicationStatus {
	APPROVED("승인"),
	PENDING("대기 중"),
	REJECTED("거절");

	private final String label;
	
	ApplicationStatus(String label){
		this.label = label;
	}
}
