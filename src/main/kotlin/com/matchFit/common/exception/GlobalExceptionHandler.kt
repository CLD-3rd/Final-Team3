package com.matchFit.common.exception

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.dto.response.ApiResponseDTO
import com.matchFit.user.exception.NicknameAlreadyExistException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(GeneralException::class)
    fun handleGeneralException(ex: GeneralException): ResponseEntity<ApiResponseDTO<Void>> {
        val errorCode = ex.errorCode

        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ApiResponseDTO.onFailure(errorCode, null))
    }

    @ExceptionHandler(NicknameAlreadyExistException::class)
    fun handleNicknameDup(@Suppress("UNUSED_PARAMETER") ex: NicknameAlreadyExistException): ResponseEntity<ApiResponseDTO<Void>> {
        return ResponseEntity
            .status(ErrorCode.NICKNAME_DUPLICATION.httpStatus)
            .body(ApiResponseDTO.onFailure(ErrorCode.NICKNAME_DUPLICATION, null))
    }
}
