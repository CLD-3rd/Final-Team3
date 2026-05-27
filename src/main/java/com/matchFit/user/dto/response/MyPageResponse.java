package com.matchFit.user.dto.response;

import com.matchFit.post.entity.Sports;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPageResponse {
    private final String email;
    private final String nickName;
    private final String town;
    private final int age;
    private final Sports sports;
}
