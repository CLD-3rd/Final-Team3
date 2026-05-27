package com.matchFit.follow.repository;

import com.matchFit.follow.entity.Follow;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("SELECT f FROM Follow f JOIN FETCH f.post WHERE f.user.id = :userId")
    List<Follow> findByUserIdWithPost(@Param("userId") Long userId);

    long countByPostId(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT f.post.id FROM Follow f WHERE f.user.id = :userId")
    Set<Long> findPostIdsByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    void deleteByPostId(Long postId);
}
