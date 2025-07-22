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
	
	// 사용자의 찜 여부 확인
	boolean existsByPostIdAndUserIdAndFollowTrue(Long postId, Long userId);
	

    // 사용자가 신청한 모집글 조회 
    @Query("SELECT p FROM Participation p " +
           "JOIN FETCH p.post post " +
           "WHERE p.user.id = :userId " +
           "ORDER BY p.createdAt DESC")
    List<Participation> findByUserIdWithPost(@Param("userId") Long userId);
    
    // 특정 게시글의 승인된 참가자 수 계산
    int countByPostIdAndStatus(Long postId, ApplicationStatus status);

}
