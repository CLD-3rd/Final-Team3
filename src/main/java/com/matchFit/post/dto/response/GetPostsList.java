package com.matchFit.post.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPostsList {
    private final List<GetPost> posts;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public static GetPostsList from(List<GetPost> posts) {
        return new GetPostsList(posts, 0, posts.size(), posts.size(), 1);
    }

    public static GetPostsList of(
            List<GetPost> posts,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        return new GetPostsList(posts, page, size, totalElements, totalPages);
    }
}
