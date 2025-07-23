package com.matchFit.post.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPosts {
	private List<GetMyPost> posts;

	public static GetMyPosts of(List<GetMyPost> myPosts) {
		return new GetMyPosts(myPosts);
	}
}
