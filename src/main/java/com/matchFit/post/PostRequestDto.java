package com.matchFit.post;

import java.time.LocalDateTime;

public class PostRequestDto {
	private Long id;
	private String title;
	private String description;
	private String imageUrl;
	private int cost;
	
	private int maxPeople;
	private LocalDateTime date;
}
