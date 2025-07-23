package com.matchFit.post.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPostApplicants {
	private List<GetMyPostApplicant> applicants;

	public static GetMyPostApplicants of(List<GetMyPostApplicant> applicantDtos) {
		return new GetMyPostApplicants(applicantDtos);
	}
}
