package com.matchFit.post.repository;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findAllByDateBetween(LocalDateTime start, LocalDateTime end);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("update Post p set p.status = :expired where p.date < :now and p.status = :open")
    int markExpired(
            @Param("now") LocalDateTime now,
            @Param("expired") Status expired,
            @Param("open") Status open
    );
}
