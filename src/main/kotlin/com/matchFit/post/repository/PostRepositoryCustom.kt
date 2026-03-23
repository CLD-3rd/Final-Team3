package com.matchFit.post.repository

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.Sports
import com.matchFit.user.entity.Gender
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface PostRepositoryCustom {
    fun findByFilters(sports: Sports?, gender: Gender?, date: LocalDate?, pageable: Pageable): Page<Post>
    fun findByFiltersAndIds(sports: Sports?, gender: Gender?, date: LocalDate?, ids: Collection<Long>): List<Post>
}
