package com.matchFit.post.repository

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.QPost
import com.matchFit.post.entity.Sports
import com.matchFit.user.entity.Gender
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.LocalDateTime

class PostRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositoryCustom {

    private fun buildFilterCondition(sports: Sports?, gender: Gender?, date: LocalDate?): BooleanBuilder {
        val post = QPost.post
        val builder = BooleanBuilder()

        sports?.let { builder.and(post.sports.eq(it)) }
        gender?.let { builder.and(post.gender.eq(it)) }

        if (date == null) {
            builder.and(post.date.gt(LocalDateTime.now()))
        } else {
            builder.and(post.date.goe(date.atStartOfDay()))
            builder.and(post.date.lt(date.plusDays(1).atStartOfDay()))
        }

        return builder
    }

    override fun findByFilters(sports: Sports?, gender: Gender?, date: LocalDate?, pageable: Pageable): Page<Post> {
        val post = QPost.post
        val condition = buildFilterCondition(sports, gender, date)

        val content = queryFactory
            .selectFrom(post)
            .where(condition)
            .orderBy(post.date.asc(), post.id.asc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .where(condition)
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findByFiltersAndIds(sports: Sports?, gender: Gender?, date: LocalDate?, ids: Collection<Long>): List<Post> {
        val post = QPost.post
        val condition = buildFilterCondition(sports, gender, date)
        condition.and(post.id.`in`(ids))

        return queryFactory
            .selectFrom(post)
            .where(condition)
            .fetch()
    }

}
