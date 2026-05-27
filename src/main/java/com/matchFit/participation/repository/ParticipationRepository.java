package com.matchFit.participation.repository;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    int countByPostId(Long postId);

    int countByPost_IdAndStatus(Long postId, ApplicationStatus status);

    @Query("""
            SELECT p FROM Participation p
            JOIN FETCH p.user
            WHERE p.post.id = :postId
            """)
    List<Participation> findAllByPostIdWithUser(@Param("postId") Long postId);

    @Query("""
            SELECT p FROM Participation p
            JOIN FETCH p.post post
            WHERE p.user.id = :userId
            ORDER BY p.createdAt DESC
            """)
    List<Participation> findByUserIdWithPost(@Param("userId") Long userId);

    Participation findByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT p.post.id, COUNT(p) FROM Participation p WHERE p.status = :status AND p.post.id IN :postIds GROUP BY p.post.id")
    List<Object[]> countApprovedByPostIds(
            @Param("postIds") List<Long> postIds,
            @Param("status") ApplicationStatus status
    );

    void deleteByPostId(Long postId);
}
