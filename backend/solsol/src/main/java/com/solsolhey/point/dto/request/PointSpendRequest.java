package com.solsolhey.point.dto.request;

import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 포인트 사용 요청 DTO
 */
public record PointSpendRequest(
    @NotNull(message = "포인트 금액은 필수입니다.")
    @Min(value = 1, message = "포인트 금액은 1 이상이어야 합니다.")
    Integer pointAmount,
    
    @Size(max = 200, message = "설명은 200자 이하여야 합니다.")
    String description,
    
    Long referenceId,
    
    ReferenceType referenceType
) {
    public PointSpendRequest {
        if (pointAmount == null || pointAmount < 1) {
            throw new IllegalArgumentException("포인트 금액은 1 이상이어야 합니다.");
        }
        
        // 참조 타입이 있으면 참조 ID도 필요
        if (referenceType != null && referenceId == null) {
            throw new IllegalArgumentException("참조 타입이 있을 때는 참조 ID가 필요합니다.");
        }
    }
}
