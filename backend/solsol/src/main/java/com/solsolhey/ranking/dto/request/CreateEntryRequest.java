package com.solsolhey.ranking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 랭킹 참가 등록 요청 DTO
 */
public record CreateEntryRequest(
    
    @NotNull(message = "마스코트 스냅샷 ID는 필수입니다")
    Long mascotSnapshotId,
    
    @NotBlank(message = "참가 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "참가 제목은 1자 이상 100자 이하여야 합니다")
    String title,
    
    @Size(max = 500, message = "참가 설명은 500자 이하여야 합니다")
    String description
) {
    
    /**
     * 컴팩트 생성자로 입력 데이터 검증
     */
    public CreateEntryRequest {
        if (mascotSnapshotId == null || mascotSnapshotId <= 0) {
            throw new IllegalArgumentException("마스코트 스냅샷 ID는 0보다 큰 값이어야 합니다");
        }
        
        if (score == null || score < 0 || score > 10000) {
            throw new IllegalArgumentException("랭킹 점수는 0 이상 10000 이하여야 합니다");
        }
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("참가 제목은 필수입니다");
        }
        
        if (title.length() > 100) {
            throw new IllegalArgumentException("참가 제목은 100자 이하여야 합니다");
        }
        
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("참가 설명은 500자 이하여야 합니다");
        }
    }
}
