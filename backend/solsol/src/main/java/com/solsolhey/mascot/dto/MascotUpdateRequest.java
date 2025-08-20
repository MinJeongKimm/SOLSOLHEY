package com.solsolhey.mascot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotUpdateRequest {
    
    @Size(min = 1, max = 50, message = "마스코트 이름은 1자 이상 50자 이하여야 합니다")
    private String name;
    
    @Size(max = 100, message = "장착 아이템은 100자 이하여야 합니다")
    private String equippedItem;
    
    @Min(value = 0, message = "경험치는 0 이상이어야 합니다")
    private Integer expToAdd;
}
