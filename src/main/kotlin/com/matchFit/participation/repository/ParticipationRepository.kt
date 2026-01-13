package com.matchFit.participation.repository

import com.matchFit.participation.entity.ApplicationStatus
import com.matchFit.participation.entity.Participation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ParticipationRepository : JpaRepository<Participation, Long> {
    fun countByPostId(postId: Long): Int

    fun countByPost_IdAndStatus(postId: Long, status: ApplicationStatus): Int

    fun findAllByPostId(postId: Long): List<Participation>

    @Query(
        """
        SELECT p FROM Participation p
        JOIN FETCH p.post post
        WHERE p.user.id = :userId
        ORDER BY p.createdAt DESC
        """
    )
    fun findByUserIdWithPost(@Param("userId") userId: Long): List<Participation>

    fun findByPostIdAndUserId(postId: Long, userId: Long): Participation?

    @Query(
        "SELECT p.post.id, COUNT(p) FROM Participation p WHERE p.status = :status AND p.post.id IN :postIds GROUP BY p.post.id"
    )
    fun countApprovedByPostIds(
        @Param("postIds") postIds: List<Long>,
        @Param("status") status: ApplicationStatus
    ): List<Array<Any>>

    fun deleteByPostId(postId: Long)
}
