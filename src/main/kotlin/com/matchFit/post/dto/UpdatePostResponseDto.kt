package com.matchFit.post.dto

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.Sports
import com.matchFit.post.entity.Status
import com.matchFit.post.entity.Town
import com.matchFit.user.entity.Gender
import java.time.LocalDateTime

data class UpdatePostResponseDto(
    val title: String,
    val description: String,
    val location: String,
    val date: LocalDateTime,
    val maxPeople: Int,
    val gender: Gender,
    val status: Status,
    val cost: Int,
    val imageUrl: String?,
    val sports: Sports,
    val town: Town,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(post: Post): UpdatePostResponseDto =
            UpdatePostResponseDto(
                post.title,
                post.description,
                post.location,
                post.date,
                post.maxPeople,
                post.gender,
                post.status,
                post.cost,
                post.imageUrl,
                post.sports,
                post.town,
                post.createdAt,
                post.updatedAt
            )
    }
}
