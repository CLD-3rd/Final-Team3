package com.matchFit.post.entity;

import java.time.LocalDateTime;

import com.matchFit.common.BaseEntity;
import com.matchFit.user.entity.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
		
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Sports sports;

	
	@Column(nullable = false)
	private Integer cost;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Town town;
	
	@Column(nullable = false)
	private Integer maxPeople;
	
	@Column(nullable = false)
	private LocalDateTime date;
	
}
