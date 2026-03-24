package com.matchFit.post.dto.response

data class GetMyPostApplicants(
    val applicants: List<GetMyPostApplicant>
) {
    companion object {
        fun from(applicantDtos: List<GetMyPostApplicant>): GetMyPostApplicants {
            return GetMyPostApplicants(applicantDtos)
        }
    }
}
