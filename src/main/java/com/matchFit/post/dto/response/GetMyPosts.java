package com.matchFit.post.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPosts {
    private final List<GetMyPost> posts;

    public static GetMyPosts from(List<GetMyPost> myPosts) {
        return new GetMyPosts(myPosts);
    }
}
