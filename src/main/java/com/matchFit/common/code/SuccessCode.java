package com.matchFit.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
	
	// POST
	POST_CREATED("POST200", "모집글이 성공적으로 생성되었습니다."),
	POST_GET_DETAIL("POST201", "모집글 정보를 성공적으로 조회했습니다."),
	POST_APPLY("POST202", "모집글에 성공적으로 신청했습니다."),
	POST_GET_LIST("POST203", "모집글 리스트를 성공적으로 조회했습니다."),
	POST_GET_CALENDER("POST204", "모집글 캘린더를 성공적으로 조회했습니다."),
	POST_GET_MY_POSTS("POST205", "내 모집글 리스트를 성공적으로 조회했습니다."),
	POST_GET_MY_APPLICANTS("POST206", "내 모집글 신청자 리스트를 성공적으로 조회했습니다."),
	POST_UPDATED("POST207", "모집글이 성공적으로 수정되었습니다."),
	POST_GET_MY_APPLIED_POSTS("POST208", "내가 신청한 모집글 리스트를 성공적으로 조회했습니다."),
	
	// USER
    USER_CREATED("USER200", "회원가입이 성공적으로 완료되었습니다."),
    USER_LOGIN_SUCCESS("USER201", "로그인에 성공했습니다.");
	
	
	private final String code;
	private final String message;

}
