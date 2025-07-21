package com.matchFit.post.entity;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum Sports {
	FOOTBALL("축구"),
	BASKETBALL("농구"),
	BADMINTON("배드민턴"),
	TABLE_TENNIS("탁구"),
	VOLLEYBALL("배구"),
	TENNIS("테니스");
	
	private final String label;
	private static final Map<String, Sports> LABEL_MAP = new HashMap<>();
	
	static {
        for (Sports s : values()) {
            LABEL_MAP.put(s.label, s);
        }
    }
	
	Sports(String label) {
		this.label = label;
	}

	public static Sports fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        Sports s = LABEL_MAP.get(label);
        if (s == null) {
            throw new IllegalArgumentException("Unknown sports: " + label);
        }
        return s;
    }
}
