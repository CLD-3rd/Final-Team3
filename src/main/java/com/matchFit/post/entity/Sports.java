package com.matchFit.post.entity;

public enum Sports {
	FOOTBALL("축구"),
	BASKETBALL("농구"),
	BADMINTON("배드민턴"),
	TABLE_TENNIS("탁구"),
	VOLLEYBALL("배구"),
	TENNIS("테니스");
	
	private final String label;
	
	Sports(String label) {
		this.label = label;
	}
}
