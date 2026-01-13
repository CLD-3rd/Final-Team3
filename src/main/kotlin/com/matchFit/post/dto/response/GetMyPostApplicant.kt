package com.matchFit.post.dto.response

import com.matchFit.participation.entity.Participation


data class GetMyPostApplicant(
    val userId: Long,
    val nickname: String,
    val gender: String,
    val age: Int,
    val status: String
) {
    companion object {
        fun from(applicants: List<Participation>): List<GetMyPostApplicant> =
            applicants.map { applicant ->
                GetMyPostApplicant(
                    requireNotNull(applicant.user.id),
                    applicant.user.nickname,
                    applicant.user.gender.label,
                    applicant.user.age,
                    applicant.status.toString()
                )
            }
    }
}
