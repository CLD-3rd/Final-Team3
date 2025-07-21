package com.matchFit.post.dto;

import java.time.LocalDateTime;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Town;
import com.matchFit.post.entity.Status;
import com.matchFit.user.entity.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {
	private Long id;
	private String title;
	private String description;
	private String location;
	private String imageUrl;
	private Gender gender;
	private Sports sports;
	private Integer cost;
	private Status status;
	private Town town;
	private Integer maxPeople;
	private LocalDateTime date;
	
	public Post toEntity() {
		return Post.builder()
				.title(this.title)
				.description(this.description)
				.location(this.location)
				.imageUrl(this.imageUrl)
	            .gender(this.gender)
	            .sports(this.sports)
	            .cost(this.cost)
	            .status(this.status)
	            .town(this.town)
	            .maxPeople(this.maxPeople)
	            .date(this.date)
	            .build();
	}
}
