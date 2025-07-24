package com.matchFit.common.dto.response;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.code.SuccessCode;

import lombok.Getter;

@Getter
public class ApiResponseDTO<T> {

    private final String code;
    private final String message;
    private final T data;

    private ApiResponseDTO(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseDTO<T> onSuccess(T data) {
        return new ApiResponseDTO<>("SUCCESS", "요청이 정상 처리되었습니다.", data);
    }
    

    public static <T> ApiResponseDTO<T> onSuccess(SuccessCode code, T data) {
        return new ApiResponseDTO<>(code.getCode(), code.getMessage(), data);
    }

    //TODO FaliureCode 생성 후 변경
    public static <T> ApiResponseDTO<T> onFailure(ErrorCode code, T data) {
        return new ApiResponseDTO<>(code.getCode(), code.getMessage(), data);
    }

//    public static <T> ApiResponseDTO<T> onFailure(ErrorCode code) {
//        return new ApiResponseDTO<>(code.getCode(), code.getMessage());
//    }

}
