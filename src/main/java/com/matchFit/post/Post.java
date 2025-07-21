package com.matchFit.post;

import java.time.LocalDateTime;

import com.matchFit.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Post extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false)
	private String description;

	@Column(nullable = true)
	private String imageUrl;
	
	@Column(nullable = false)
	private GENDER gender;
	
	@Column(nullable = false)
	private SPORTS sports;
	
	@Column(nullable = false)
	private Integer cost;
	
	private STATUS status;
	
//	@Column(nullable = false)
//	private TOWN town;
	
	@Column(nullable = false)
	private Integer maxPeople;
	
	@Column(nullable = false)
	private LocalDateTime date;
	
}
