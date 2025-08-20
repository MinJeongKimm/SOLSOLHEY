package com.solsolhey.solsol.dto.share;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 공유 이미지 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ShareImageCreateRequest {

    @NotBlank(message = "템플릿 이름은 필수입니다.")
    private String template;

    @NotNull(message = "이미지 데이터는 필수입니다.")
    private Map<String, Object> data; // 이미지 생성에 필요한 데이터

    public ShareImageCreateRequest(String template, Map<String, Object> data) {
        this.template = template;
        this.data = data;
    }
}
