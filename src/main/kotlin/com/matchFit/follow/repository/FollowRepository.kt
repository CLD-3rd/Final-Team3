package com.matchFit.follow.repository

import com.matchFit.follow.entity.Follow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FollowRepository : JpaRepository<Follow, Long> {
    @Query("SELECT f FROM Follow f JOIN FETCH f.post WHERE f.user.id = :userId")
    fun findByUserIdWithPost(@Param("userId") userId: Long): List<Follow>

    fun countByPostId(postId: Long): Long

    fun existsByUserIdAndPostId(userId: Long, postId: Long): Boolean

    fun deleteByUserIdAndPostId(userId: Long, postId: Long)

    fun deleteByPostId(postId: Long)
}
