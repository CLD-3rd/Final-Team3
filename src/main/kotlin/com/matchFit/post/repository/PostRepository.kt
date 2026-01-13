package com.matchFit.post.repository

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime


interface PostRepository : JpaRepository<Post, Long> {
    @Query(
        value = """
            SELECT *
              FROM post p
             WHERE (:sports IS NULL OR p.sports = :sports)
               AND (:gender IS NULL OR p.gender = :gender)
               AND (
                     :date IS NULL AND p.date > NOW()
                  OR :date IS NOT NULL AND p.date >= :date AND p.date < :date + INTERVAL '1' DAY
                   )
             ORDER BY p.date ASC
            """,
        countQuery = """
            SELECT count(*)
              FROM post p
             WHERE (:sports IS NULL OR p.sports = :sports)
               AND (:gender IS NULL OR p.gender = :gender)
               AND (
                     :date IS NULL AND p.date > NOW()
                  OR :date IS NOT NULL AND p.date >= :date AND p.date < :date + INTERVAL '1' DAY
                   )
            """,
        nativeQuery = true
    )
    fun findByFilters(
        @Param("sports") sports: String?,
        @Param("gender") gender: String?,
        @Param("date") date: LocalDate?,
        pageable: Pageable
    ): Page<Post>

    fun findAllByDateBetween(start: LocalDateTime, end: LocalDateTime): List<Post>

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Post>

    fun findByStatusAndDate(closed: Status, tomorrow: LocalDate): List<Post>

    fun findByStatusAndDateBetween(status: Status, startDate: LocalDateTime, endDate: LocalDateTime): List<Post>

    @Modifying
    @Query("update Post p set p.status = :expired where p.date < :now and p.status = :open")
    fun markExpired(
        @Param("now") now: LocalDateTime,
        @Param("expired") expired: Status,
        @Param("open") open: Status
    ): Int
}
