package com.matchFit.post.dto.response;

import java.util.List;

import lombok.Getter;

@Getter
public class GetPostsList {
    private List<GetPost> posts;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    // 페이징 정보 없는 경우
    public GetPostsList(List<GetPost> posts) {
        this.posts = posts;
        this.page = 0;
        this.size = posts.size();
        this.totalElements = posts.size();
        this.totalPages = 1;
    }

    // 페이징 정보 있는 경우
    public GetPostsList(List<GetPost> posts, int page, int size, long totalElements, int totalPages) {
        this.posts = posts;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static GetPostsList of(List<GetPost> posts) {
        return new GetPostsList(posts);
    }

    public static GetPostsList of(List<GetPost> posts, int page, int size, long totalElements, int totalPages) {
        return new GetPostsList(posts, page, size, totalElements, totalPages);
    }
}
