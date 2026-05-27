package com.matchFit.follow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyFollowResponseDto {
    private final Long postId;
    private final String title;
    private final String sports;
    private final String location;
    private final String date;
    private final int currentPeople;
    private final int maxPeople;
    private final String followedAt;
    private final int cost;
    private final String status;
}
