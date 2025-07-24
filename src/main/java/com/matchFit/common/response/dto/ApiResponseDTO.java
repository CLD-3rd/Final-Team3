package com.matchFit.common.response.dto;

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

    public static <T> ApiResponseDTO<T> onSuccess(String message, T data) {
        return new ApiResponseDTO<>("SUCCESS", message, data);
    }

    public static <T> ApiResponseDTO<T> onFailure(String code, String message, T data) {
        return new ApiResponseDTO<>(code, message, data);
    }

    public static <T> ApiResponseDTO<T> onFailure(String code, String message) {
        return new ApiResponseDTO<>(code, message, null);
    }

    // Getter (Lombok 써도 됨)
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
