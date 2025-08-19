package com.solsolhey.solsol.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * API 공통 응답 포맷
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    @Builder
    private ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("요청이 성공적으로 처리되었습니다.")
                .build();
    }
    
    /**
     * 성공 응답 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 생성 성공 응답
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message("리소스가 성공적으로 생성되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 생성 성공 응답 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 실패 응답
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .build();
    }
    
    /**
     * 실패 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 잘못된 요청 응답
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * 인증 실패 응답
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message != null ? message : "인증이 필요합니다.");
    }
    
    /**
     * 접근 금지 응답
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message != null ? message : "접근 권한이 없습니다.");
    }
    
    /**
     * 리소스 없음 응답
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message != null ? message : "요청한 리소스를 찾을 수 없습니다.");
    }
    
    /**
     * 서버 내부 오류 응답
     */
    public static <T> ApiResponse<T> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, 
                    message != null ? message : "서버 내부 오류가 발생했습니다.");
    }
}
