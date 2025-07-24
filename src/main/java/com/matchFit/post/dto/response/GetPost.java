package com.matchFit.post.dto.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.matchFit.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class GetPost {

    private String title;
    private String sports;
    private String date;        // 날짜 형식은 String으로 했지만, 필요하면 LocalDateTime 등으로 변경 가능
    private String status;
    private String town;
    private Integer maxPeople;
    private String gender;
    private Long viewCount; // 게시글 조회수

	public static List<GetPost> from(List<Post> posts, Map<Long, Long> viewCounts) {
		return posts.stream()
	            .map(post -> new GetPost(
	                post.getTitle(),
	                post.getSports().getLabel(),
	                post.getDate().toString(),
	                post.getStatus().getLabel(),
	                post.getTown().getLabel(),
	                post.getMaxPeople(),
	                post.getGender().getLabel(),
	                viewCounts.getOrDefault(post.getId(), 0L)
	            ))
	            .collect(Collectors.toList());
		}

}
