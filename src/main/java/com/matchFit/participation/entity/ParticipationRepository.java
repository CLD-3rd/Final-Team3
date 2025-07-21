package com.matchFit.participation.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
	// 현재 신청자 수 계산
	int countByPostId(Long postId);
	
	// 사용자의 찜 여부 확인
	boolean existsByPostIdAndUserIdAndFollowTrue(Long postId, Long userId);
	
}
