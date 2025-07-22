package com.matchFit.post.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetPostsList {
	private List<GetPost> posts;
	
	public static GetPostsList of(List<GetPost> posts) {
		return new GetPostsList(posts);
	}
}
