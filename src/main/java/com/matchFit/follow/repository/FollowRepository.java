package com.matchFit.follow.repository;

import com.matchFit.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    // 내 모든 팔로우 조회
    @Query("SELECT f FROM Follow f JOIN FETCH f.post WHERE f.user.id = :userId")
    List<Follow> findByUserIdWithPost(@Param("userId") Long userId);
    
    // 게시글의 팔로우 개수
    long countByPostId(Long postId);
    
    // 팔로우 상태 확인 
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    // 팔로우 삭제 
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
