package com.solsolhey.mascot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotEquipRequest {
    
    @NotBlank(message = "장착할 아이템은 필수입니다")
    @Size(min = 1, max = 100, message = "장착 아이템은 1자 이상 100자 이하여야 합니다")
    private String equippedItem;
}
