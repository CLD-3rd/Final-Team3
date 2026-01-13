package com.matchFit.participation.dto.request

class ManageApplicant(
    val applicantId: Long,
    val decision: Decision
) {
    enum class Decision {
        ACCEPT,
        REJECT
    }
}
