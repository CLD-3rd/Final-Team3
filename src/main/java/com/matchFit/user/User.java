package com.matchFit.user;

import com.matchFit.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	
//	@Column(nullable = false)
//	private GENDER gender;
	
	@Column(nullable = false)
	private Integer age;
	
//	@Column(nullable = false)
//	private SPORTS sports;
	
	@Column(nullable = false)
	private String town;
	
	@Column(nullable = false)
	private Integer recruitCount = 0;
	
	@Column(nullable = false)
	private Integer joinCount = 0;
}
