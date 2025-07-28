package com.matchFit.post.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPost {
	private Long postId;
	private String title;
    private LocalDateTime date;
    private Integer currentPeople;
    private Integer maxPeople;
    private String status;
}
