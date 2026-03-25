package com.matchFit.post.dto

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.Sports
import com.matchFit.post.entity.Status
import com.matchFit.post.entity.Town
import com.matchFit.user.entity.Gender
import java.time.LocalDateTime

class PostInfoResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val gender: Gender,
    val sports: Sports,
    val cost: Int,
    val status: Status,
    val town: Town,
    val maxPeople: Int,
    val date: LocalDateTime,
    val location: String,
    val currentPeople: Int,
    val isBookmarked: Boolean,
    val userEmail: String
) {
    constructor(post: Post, currentPeople: Int, isBookmarked: Boolean) : this(
        post.id!!,
        post.title,
        post.description,
        post.imageUrl,
        post.gender,
        post.sports,
        post.cost,
        post.status,
        post.town,
        post.maxPeople,
        post.date,
        post.location,
        currentPeople,
        isBookmarked,
        post.user.email
    )
}
