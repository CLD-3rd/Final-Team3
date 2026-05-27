package com.matchFit.user.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    ALL("남녀 모두");

    private static final Map<String, Gender> LABEL_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(Gender::getLabel, Function.identity()));

    private final String label;

    public static Gender fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        Gender value = LABEL_MAP.get(label);
        if (value == null) {
            throw new IllegalArgumentException("Unknown gender: " + label);
        }
        return value;
    }
}
