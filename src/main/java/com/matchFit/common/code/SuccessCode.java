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
	POST_CANCEL_APPLY("POST209", "모집글 신청이 성공적으로 취소되었습니다."),

	// USER
    USER_CREATED("USER200", "회원가입이 성공적으로 완료되었습니다."),
    USER_LOGIN_SUCCESS("USER201", "로그인에 성공했습니다."), 
    USER_EMAIL_AVAILABLE("USER202", "사용 가능한 이메일입니다."),
    USER_NICKNAME_AVAILABLE("USER203", "사용 가능한 닉네임입니다."),
    USER_GET_MY_PROFILE("USER204", "사용자 프로필 정보를 성공적으로 조회했습니다."),
	USER_EDIT_MY_PROFILE("USER205", "사용자 프로필 정보가 성공적으로 수정되었습니다."),
	USER_GET_MY_FOLLOWS("USER206", "내가 찜한 모집글 리스트를 성공적으로 조회했습니다."),

	
	// PARTICIPATION
	PARTICIPATION_MANAGED("PARTICIPATION200", "신청자 관리가 성공적으로 완료되었습니다."),
	
	// FOLLOW
	FOLLOW_TOGGLED("FOLLOW200", "찜 상태가 성공적으로 변경되었습니다.");
	
	private final String code;
	private final String message;

}
