package com.matchFit.common.code

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    // POST
    POST_UNAUTHORIZED_USER("POST400", "게시글 작성 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    POST_PAST_EVENT_MODIFICATION("POST401", "과거 날짜에 게시글을 작성할 수 없습니다.", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND("POST402", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND),
    INVALID_SORTING_TYPE("POST403", "유효하지 않은 정렬 타입입니다.", HttpStatus.BAD_REQUEST),
    POST_PAST_DAYS("POST404", "과거 날짜에 게시글을 조회할 수 없습니다.", HttpStatus.BAD_REQUEST),
    POST_PAST_MONTHS("POST405", "과거 달에 게시글을 조회할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // PARTICIPATION
    PARTICIPATION_CANCELLATION_TIME_EXCEEDED("PARTICIPATION400", "경기 하루 전부터는 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // USER
    EMAIL_DUPLICATION("USER400", "이미 사용중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_DUPLICATION("USER401", "이미 사용중인 닉네임입니다.", HttpStatus.BAD_REQUEST),
    GENDER_INVALID("USER402", "유효하지 않은 성별입니다.", HttpStatus.BAD_REQUEST),
    SPORTS_INVALID("USER403", "유효하지 않은 운동입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER404", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID("USER405", "유효하지 않은 비밀번호입니다.", HttpStatus.BAD_REQUEST),
    KAKAO_LOGIN_FAILED("USER405", "카카오 계정으로 로그인한 사용자입니다. 일반 로그인은 불가능합니다.", HttpStatus.UNAUTHORIZED),
    NICKNAME_NOT_FOUND("USER406", "존재하지 않는 닉네임입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME("USER407", "이전 비밀번호와 동일합니다.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND("USER408", "가입된 이메일이 아닙니다.", HttpStatus.BAD_REQUEST)
}
