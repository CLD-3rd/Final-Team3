package com.matchFit.post.dto.response

import com.matchFit.post.entity.Post


data class GetPost(
    val id: Long,
    val title: String,
    val sports: String,
    val date: String,
    val status: String,
    val town: String,
    val cost: Int,
    val currentPeople: Int,
    val maxPeople: Int,
    val gender: String,
    val viewCount: Long
) {
    companion object {
        fun from(
            posts: List<Post>,
            viewCounts: Map<Long, Long>,
            currentPeopleMap: Map<Long, Int>
        ): List<GetPost> =
            posts.map { post ->
                val postId = requireNotNull(post.id)
                GetPost(
                    postId,
                    post.title,
                    post.sports.label,
                    post.date.toString(),
                    post.status.label,
                    post.town.label,
                    post.cost,
                    currentPeopleMap[postId] ?: 0,
                    post.maxPeople,
                    post.gender.label,
                    viewCounts[postId] ?: 0L
                )
            }
    }
}
