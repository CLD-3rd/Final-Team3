package com.matchFit.common.dto.response

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.code.SuccessCode

data class ApiResponseDTO<T>(
    val code: String,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> onSuccess(data: T?): ApiResponseDTO<T> =
            ApiResponseDTO("SUCCESS", "요청이 정상 처리되었습니다.", data)

        fun <T> onSuccess(code: SuccessCode, data: T?): ApiResponseDTO<T> =
            ApiResponseDTO(code.code, code.message, data)

        fun <T> onFailure(code: ErrorCode, data: T?): ApiResponseDTO<T> =
            ApiResponseDTO(code.code, code.message, data)
    }
}
