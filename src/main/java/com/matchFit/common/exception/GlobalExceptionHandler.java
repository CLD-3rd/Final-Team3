package com.matchFit.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.dto.response.ApiResponseDTO;

public class GlobalExceptionHandler {
	// 이 한 군데로 CustomException을 상속한 모든 예외를 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleCustomException(GeneralException ex) {
        ErrorCode error = ex.getErrorCode();
        return ResponseEntity
                .status(error.getHttpStatus())
                .body(ApiResponseDTO.onFailure(error.getCode(), error.getMessage(), null));
    }

}
