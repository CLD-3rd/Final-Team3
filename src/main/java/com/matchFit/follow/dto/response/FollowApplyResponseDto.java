package com.matchFit.follow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowApplyResponseDto {
    private final Long postId;
    private final boolean isFollowed;
}
