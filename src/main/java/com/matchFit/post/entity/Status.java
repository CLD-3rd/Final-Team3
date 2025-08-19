package com.matchFit.post.entity;

import lombok.Getter;

@Getter
public enum Status {
	OPEN("모집중"),
	CLOSED("모집완료"),
	EXPIRED("모집만료");   // 새 값 추가

	private final String label;

	Status(String label) {
		this.label = label;
	}

}
