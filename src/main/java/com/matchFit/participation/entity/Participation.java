package com.matchFit.participation.entity;

import com.matchFit.common.entity.BaseEntity;
import com.matchFit.post.entity.Post;
import com.matchFit.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class Participation extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postId", nullable = false)
	private Post post;
	
	// 신청 상태의 status, 모집 상태의 status랑 구분
	@Enumerated(EnumType.STRING)
	private ApplicationStatus status;
	
	private boolean follow;
	
	protected Participation() {}
	
	public Participation(User user, Post post) {
		this.user = user;
		this.post = post;
		this.status = ApplicationStatus.PENDING;
	}
	
	
}
