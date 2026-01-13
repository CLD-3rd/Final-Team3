package com.matchFit.post.dto.response

import java.time.LocalDateTime

data class GetMyPost(
    val postId: Long,
    val title: String,
    val date: LocalDateTime,
    val currentPeople: Int,
    val maxPeople: Int,
    val status: String
)
