package com.matchFit.participation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
	// 현재 신청자 수 계산
	int countByPostId(Long postId);
	
	// 신청 승인된 사용자만 count
	int countByPost_IdAndStatus(Long postId, ApplicationStatus status);
	
	
	List<Participation> findAllByPostId(Long postId);


	// 사용자가 신청한 모집글 조회 
  @Query("SELECT p FROM Participation p " +
         "JOIN FETCH p.post post " +
         "WHERE p.user.id = :userId " +
         "ORDER BY p.createdAt DESC")
  List<Participation> findByUserIdWithPost(@Param("userId") Long userId);
  
  
	Participation findByPostIdAndUserId(Long postId, Long userId);

	int countByPost_Id(Long postId);
	
}

