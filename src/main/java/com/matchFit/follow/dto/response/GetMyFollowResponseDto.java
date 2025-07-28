package com.matchFit.follow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetMyFollowResponseDto {
    private Long postId;
    private String title;
    private String sports;          
    private String location;        
    private String date;            
    private int currentPeople;    
    private int maxPeople;        
    private String followedAt;      
    private Integer cost;          
    private String status;         
}