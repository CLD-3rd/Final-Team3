package com.matchFit.post.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sports {
    FOOTBALL("축구"),
    BASKETBALL("농구"),
    BADMINTON("배드민턴"),
    TABLE_TENNIS("탁구"),
    VOLLEYBALL("배구"),
    TENNIS("테니스");

    private static final Map<String, Sports> LABEL_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(Sports::getLabel, Function.identity()));

    private final String label;

    public static Sports fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        Sports value = LABEL_MAP.get(label);
        if (value == null) {
            throw new IllegalArgumentException("Unknown sports: " + label);
        }
        return value;
    }
}
