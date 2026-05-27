package com.matchFit.post.dto.response;

import com.matchFit.post.entity.Post;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPost {
    private final Long id;
    private final String title;
    private final String sports;
    private final String date;
    private final String status;
    private final String town;
    private final int cost;
    private final int currentPeople;
    private final int maxPeople;
    private final String gender;
    private final long viewCount;
    private final boolean isFollowed;

    public static List<GetPost> of(
            List<Post> posts,
            Map<Long, Long> viewCounts,
            Map<Long, Integer> currentPeopleMap,
            Set<Long> followedPostIds
    ) {
        Set<Long> followSet = followedPostIds == null ? Collections.emptySet() : followedPostIds;
        return posts.stream()
                .map(post -> {
                    Long postId = Objects.requireNonNull(post.getId());
                    return new GetPost(
                            postId,
                            post.getTitle(),
                            post.getSports().getLabel(),
                            post.getDate().toString(),
                            post.getStatus().getLabel(),
                            post.getTown().getLabel(),
                            post.getCost(),
                            currentPeopleMap.getOrDefault(postId, 0),
                            post.getMaxPeople(),
                            post.getGender().getLabel(),
                            viewCounts.getOrDefault(postId, 0L),
                            followSet.contains(postId)
                    );
                })
                .collect(Collectors.toList());
    }

    public static List<GetPost> of(
            List<Post> posts,
            Map<Long, Long> viewCounts,
            Map<Long, Integer> currentPeopleMap
    ) {
        return of(posts, viewCounts, currentPeopleMap, Collections.emptySet());
    }
}
