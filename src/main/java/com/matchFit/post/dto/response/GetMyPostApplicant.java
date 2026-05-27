package com.matchFit.post.dto.response;

import com.matchFit.participation.entity.Participation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMyPostApplicant {
    private final Long userId;
    private final String nickname;
    private final String gender;
    private final int age;
    private final String status;

    public static List<GetMyPostApplicant> from(List<Participation> applicants) {
        return applicants.stream()
                .map(applicant -> new GetMyPostApplicant(
                        Objects.requireNonNull(applicant.getUser().getId()),
                        applicant.getUser().getNickname(),
                        applicant.getUser().getGender().getLabel(),
                        applicant.getUser().getAge(),
                        applicant.getStatus().toString()
                ))
                .collect(Collectors.toList());
    }
}
