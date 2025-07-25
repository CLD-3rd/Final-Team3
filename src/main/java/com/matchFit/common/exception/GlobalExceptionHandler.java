package com.matchFit.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.user.exception.NicknameAlreadyExistException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	// 이 한 군데로 CustomException을 상속한 모든 예외를 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneralException(GeneralException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseDTO.onFailure(
                        errorCode,
                        null
                ));
    }
    
    @ExceptionHandler(NicknameAlreadyExistException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNicknameDup(NicknameAlreadyExistException ex) {
        return ResponseEntity
            .status(ErrorCode.NICKNAME_DUPLICATION.getHttpStatus())
            .body(ApiResponseDTO.onFailure(ErrorCode.NICKNAME_DUPLICATION, null));
    }

}

