package com.matchFit.post.dto;

import java.time.LocalDateTime;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;

import lombok.Getter;

@Getter
public class PostInfoResponseDto {
	private Long id;
	private String title;
	private String description;
	private String imageUrl;
	private Gender gender;
	private Sports sports;
	private Integer cost;
	private Status status;
	private Town town;
	private Integer maxPeople;
	private LocalDateTime date;
	private String location;
	private Integer currentPeople; // 따로 구현
	private boolean isBookmarked;  // 따로 구현
	
	public PostInfoResponseDto(Post post, int currentPeople, boolean isBookmarked) {
		this.id = post.getId();
		this.title = post.getTitle();
		this.description = post.getDescription();
		this.imageUrl = post.getImageUrl();
		this.gender = post.getGender();
		this.sports = post.getSports();
		this.cost = post.getCost();
		this.status = post.getStatus();
		this.town = post.getTown();
		this.maxPeople = post.getMaxPeople();
		this.date = post.getDate();
		this.location = post.getLocation();
		this.currentPeople = currentPeople;
		this.isBookmarked = isBookmarked;
		
		
	}
	
}
