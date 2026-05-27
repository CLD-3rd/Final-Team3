package com.matchFit.post.repository;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.QPost;
import com.matchFit.post.entity.Sports;
import com.matchFit.user.entity.Gender;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private BooleanBuilder buildFilterCondition(Sports sports, Gender gender, LocalDate date) {
        QPost post = QPost.post;
        BooleanBuilder builder = new BooleanBuilder();

        if (sports != null) builder.and(post.sports.eq(sports));
        if (gender != null) builder.and(post.gender.eq(gender));

        if (date == null) {
            builder.and(post.date.gt(LocalDateTime.now()));
        } else {
            builder.and(post.date.goe(date.atStartOfDay()));
            builder.and(post.date.lt(date.plusDays(1).atStartOfDay()));
        }

        return builder;
    }

    @Override
    public Page<Post> findByFilters(Sports sports, Gender gender, LocalDate date, Pageable pageable) {
        QPost post = QPost.post;
        BooleanBuilder condition = buildFilterCondition(sports, gender, date);

        List<Post> content = queryFactory
                .selectFrom(post)
                .where(condition)
                .orderBy(post.date.asc(), post.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalBoxed = queryFactory
                .select(post.count())
                .from(post)
                .where(condition)
                .fetchOne();
        long total = totalBoxed == null ? 0L : totalBoxed;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Post> findByFiltersAndIds(Sports sports, Gender gender, LocalDate date, Collection<Long> ids) {
        QPost post = QPost.post;
        BooleanBuilder condition = buildFilterCondition(sports, gender, date);
        condition.and(post.id.in(ids));

        return queryFactory
                .selectFrom(post)
                .where(condition)
                .fetch();
    }
}
