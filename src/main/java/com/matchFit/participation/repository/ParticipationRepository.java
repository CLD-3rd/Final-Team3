package com.matchFit.participation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matchFit.participation.entity.Participation;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
	// 현재 신청자 수 계산
	int countByPostId(Long postId);
	
	// 사용자의 찜 여부 확인
	boolean existsByPostIdAndUserIdAndFollowTrue(Long postId, Long userId);
	
	
	List<Participation> findAllByPostId(Long postId);
}
