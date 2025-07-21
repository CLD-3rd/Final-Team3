package com.matchFit.user.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String email;
    private String password;
    private String nickname;
    private Integer age;
    private String gender;
    private String town;
    private String sports;
    private boolean isKakaoUser;
}
