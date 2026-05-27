package com.matchFit.post.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPostsCalender {
    private final List<GetPostCalender> posts;

    public static GetPostsCalender from(List<GetPostCalender> postsCalender) {
        return new GetPostsCalender(postsCalender);
    }
}
