package com.matchFit.follow.dto.response

data class GetMyFollowResponseDto(
    val postId: Long,
    val title: String,
    val sports: String,
    val location: String,
    val date: String,
    val currentPeople: Int,
    val maxPeople: Int,
    val followedAt: String?,
    val cost: Int,
    val status: String
)
