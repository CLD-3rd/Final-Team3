package com.matchFit.participation.dto.response

import com.matchFit.participation.entity.ApplicationStatus

data class DecisionApplicant(
    val applicantId: Long,
    val nickName: String,
    val decision: ApplicationStatus
)
