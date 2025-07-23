package com.matchFit.post.dto.request;

import java.time.LocalDateTime;

import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditPostRequestDto {
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
}