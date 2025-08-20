package com.solsolhey.mascot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotCreateRequest {
    
    @NotBlank(message = "마스코트 이름은 필수입니다")
    @Size(min = 1, max = 50, message = "마스코트 이름은 1자 이상 50자 이하여야 합니다")
    private String name;
    
    @NotBlank(message = "마스코트 타입은 필수입니다")
    @Size(min = 1, max = 20, message = "마스코트 타입은 1자 이상 20자 이하여야 합니다")
    private String type;
    
    @Size(max = 100, message = "장착 아이템은 100자 이하여야 합니다")
    private String equippedItem;
}
