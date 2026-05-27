package com.matchFit.post.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPost {
    private final Long postId;
    private final String title;
    private final LocalDateTime date;
    private final int currentPeople;
    private final int maxPeople;
    private final String status;
}
