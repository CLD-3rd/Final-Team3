package com.matchFit.participation.dto.response

import com.matchFit.participation.entity.ApplicationStatus
import com.matchFit.post.entity.Status

data class GetMyPostsParticipationResponseDto(
    val postId: Long,
    val title: String,
    val date: String,
    val currentPeople: Int,
    val maxPeople: Int,
    val location: String,
    val cost: Int,
    val status: ApplicationStatus,
    val postStatus: Status
)
