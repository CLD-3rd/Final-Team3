package com.matchFit.post.repository;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.user.entity.Gender;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findByFilters(Sports sports, Gender gender, LocalDate date, Pageable pageable);
    List<Post> findByFiltersAndIds(Sports sports, Gender gender, LocalDate date, Collection<Long> ids);
}
