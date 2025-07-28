package com.matchFit.participation.dto.response;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.post.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetMyPostsParticipationResponseDto {
    private Long postId;           
    private String title;
    private String date;
    private int currentPeople;
    private int maxPeople;
    private String location;       
    private Integer cost;         
    private ApplicationStatus status;
    private Status postStatus;    
}