package com.matchFit.post.dto.response

data class GetMyPostApplicants(
    val applicants: List<GetMyPostApplicant>
) {
    companion object {
        fun of(applicantDtos: List<GetMyPostApplicant>) = GetMyPostApplicants(applicantDtos)
    }
}
