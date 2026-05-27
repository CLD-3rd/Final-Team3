package com.matchFit.post.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPostApplicants {
    private final List<GetMyPostApplicant> applicants;

    public static GetMyPostApplicants from(List<GetMyPostApplicant> applicantDtos) {
        return new GetMyPostApplicants(applicantDtos);
    }
}
