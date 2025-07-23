package com.matchFit.post.dto.response;

import java.util.List;

import com.matchFit.participation.entity.Participation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPostApplicant {
	private Long userId;
    private String nickname;
    private String gender;
    private Integer age;
    private String status;
	public static List<GetMyPostApplicant> from(List<Participation> applicants) {
		return applicants.stream()
				.map(applicant -> 
				new GetMyPostApplicant(
						applicant.getUser().getId(), 
						applicant.getUser().getNickname(),
						applicant.getUser().getGender().getLabel(), 
						applicant.getUser().getAge(),
						applicant.getStatus().toString()))
				.toList();
	}
}
