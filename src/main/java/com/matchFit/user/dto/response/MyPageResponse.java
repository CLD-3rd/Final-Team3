package com.matchFit.user.dto.response;

import com.matchFit.post.entity.Sports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyPageResponse {
    private String email;
    private String nickName;
    private String town;
    private Integer age;
    private Sports sports;
    private Integer recruitCount;
    private Integer joinCount;
}