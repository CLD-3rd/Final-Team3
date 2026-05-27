package com.matchFit.participation.dto.response;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.post.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPostsParticipationResponseDto {
    private final Long postId;
    private final String title;
    private final String date;
    private final int currentPeople;
    private final int maxPeople;
    private final String location;
    private final int cost;
    private final ApplicationStatus status;
    private final Status postStatus;
}
