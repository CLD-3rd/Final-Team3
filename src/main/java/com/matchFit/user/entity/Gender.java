package com.matchFit.user.entity;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum Gender {
	MALE("남성"),
    FEMALE("여성");
	
	private final String label;
	private static final Map<String, Gender> LABEL_MAP = new HashMap<>();
	
	Gender(String label) {
		this.label = label;
	}

	public String getLabel() {
        return label;
	}

	public static Gender fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        Gender g = LABEL_MAP.get(label);
        if (g == null) {
            throw new IllegalArgumentException("Unknown gender: " + label);
        }
        return g;

    }
	
}
