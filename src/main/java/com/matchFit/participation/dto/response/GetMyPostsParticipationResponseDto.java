package com.matchFit.participation.dto.response;

import com.matchFit.participation.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetMyPostsParticipationResponseDto {
    private String title;
    private String date;
    private int currentPeople;
    private int maxPeople;
    private ApplicationStatus status;
}