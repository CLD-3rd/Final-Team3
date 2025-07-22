package com.matchFit.post.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPostsCalender {
	private List<GetPostCalender> posts;

	public static GetPostsCalender of(List<GetPostCalender> postsCalender) {
		return new GetPostsCalender(postsCalender);
	}
}
