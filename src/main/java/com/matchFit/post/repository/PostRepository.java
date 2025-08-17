package com.matchFit.post.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query(value = """
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
		    nativeQuery = true)

	    Page<Post> findByFilters(
	        @Param("sports") String sports,
	        @Param("gender") String gender,
	        @Param("date") LocalDate date,
	        Pageable pageable
	    );

    
    List<Post> findAllByDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Post> findByStatusAndDate(Status closed, LocalDate tomorrow);
}

