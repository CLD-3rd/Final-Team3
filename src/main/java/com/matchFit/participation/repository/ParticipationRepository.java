package com.matchFit.participation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
	// 현재 신청자 수 계산
	int countByPostId(Long postId);
	
	// 사용자의 찜 여부 확인
	boolean existsByPostIdAndUserIdAndFollowTrue(Long postId, Long userId);
	
	// 신청 승인된 사용자만 count
	int countByPost_IdAndStatus(Long postId, ApplicationStatus status);
	
	
	List<Participation> findAllByPostId(Long postId);

	Participation findByPostIdAndUserId(Long postId, Long userId);

}
