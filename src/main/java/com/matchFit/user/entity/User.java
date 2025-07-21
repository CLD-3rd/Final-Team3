package com.matchFit.user.entity;

import com.matchFit.common.BaseEntity;
import com.matchFit.post.entity.Sports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	private String username;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;
	
	@Column(nullable = false)
	private Integer age;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Sports sports;
	
	@Column(nullable = false)
	private String town;
	
	@Column(nullable = false)
	private Integer recruitCount = 0;
	
	@Column(nullable = false)
	private Integer joinCount = 0;
}
