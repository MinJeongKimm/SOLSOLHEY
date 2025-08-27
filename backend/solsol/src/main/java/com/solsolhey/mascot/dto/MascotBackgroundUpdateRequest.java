package com.solsolhey.mascot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotBackgroundUpdateRequest {

    // 7자(예: #FFFFFF) 형식의 HEX 컬러 코드
    @NotBlank(message = "배경 색상은 필수입니다.")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "유효한 16진수 색상 코드를 입력하세요. 예: #A1B2C3")
    private String backgroundColor;

    // 패턴 타입: dots | stripes | none
    @Pattern(regexp = "^(dots|stripes|none)$", message = "패턴 타입은 dots, stripes, none 중 하나여야 합니다.")
    private String patternType = "none";
}

