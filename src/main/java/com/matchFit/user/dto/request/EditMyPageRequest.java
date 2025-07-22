package com.matchFit.user.dto.request;

import com.matchFit.post.entity.Sports;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditMyPageRequest {
    private String username;
    private Sports sports;
    private Integer age;
}