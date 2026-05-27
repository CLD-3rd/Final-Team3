package com.matchFit.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    OPEN("모집중"),
    CLOSED("모집완료"),
    EXPIRED("모집만료");

    private final String label;
}
