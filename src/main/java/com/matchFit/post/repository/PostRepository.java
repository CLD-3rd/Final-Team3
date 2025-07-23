package com.matchFit.post.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchFit.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query(value = """
	        SELECT *
	          FROM post p
	         WHERE (:sports IS NULL OR p.sports = :sports)
	           AND (:gender IS NULL OR p.gender = :gender)
	           AND (:date IS NULL OR DATE(p.date) = :date)
	         ORDER BY
	           CASE WHEN :nearest = true
	                THEN ABS(TIMESTAMPDIFF(SECOND, p.date, NOW()))
	                ELSE UNIX_TIMESTAMP(p.created_at)
	           END
	        """,
	        nativeQuery = true)
	    List<Post> findByFilters(
	        @Param("sports") String sports,
	        @Param("gender") String gender,
	        @Param("nearest") boolean nearest,
	        @Param("date") LocalDate date
	    );

    
    List<Post> findAllByDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}

