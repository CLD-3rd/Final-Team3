package com.matchFit.participation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    APPROVED("승인");

    private final String label;
}
