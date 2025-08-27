package com.solsolhey.auth.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 응답 DTO (새로운 형식)
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SignUpResponse", description = "회원가입 처리 결과. 금융 사용자 생성은 비동기로 처리되며 userKey는 내부적으로 저장됩니다.")
public class SignUpResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "메시지", example = "회원가입이 완료되었습니다.")
    private String message;

    @Schema(description = "생성된 사용자 ID", example = "123")
    private Long userId;

    @Schema(description = "가입한 이메일(표시용)", example = "user@example.com")
    private String email;

    @Schema(description = "실패 시 필드별 에러")
    private Map<String, String> errors;

    @Builder
    public SignUpResponse(boolean success, String message, Long userId, String email, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.errors = errors;
    }

    /**
     * 성공 응답 생성
     */
    public static SignUpResponse success(String message, Long userId, String email) {
        return SignUpResponse.builder()
                .success(true)
                .message(message)
                .userId(userId)
                .email(email)
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static SignUpResponse failure(String message, Map<String, String> errors) {
        return SignUpResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
