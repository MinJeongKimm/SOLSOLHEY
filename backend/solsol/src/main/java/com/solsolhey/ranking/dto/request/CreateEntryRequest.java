package com.solsolhey.ranking.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 랭킹 참가 등록 요청 DTO
 */
public record CreateEntryRequest(
    
    Long mascotSnapshotId, // 이미지 업로드 방식에서는 null 가능
    
    @NotBlank(message = "참가 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "참가 제목은 1자 이상 100자 이하여야 합니다")
    String title,
    
    @Size(max = 500, message = "참가 설명은 500자 이하여야 합니다")
    String description,
    
    String imageUrl,
    
    @NotBlank(message = "랭킹 타입은 필수입니다")
    String rankingType // "NATIONAL" 또는 "CAMPUS"
) {
    
    /**
     * 컴팩트 생성자로 입력 데이터 검증
     */
    public CreateEntryRequest {
        // mascotSnapshotId는 null이거나 0 이상이어야 함
        if (mascotSnapshotId != null && mascotSnapshotId < 0) {
            throw new IllegalArgumentException("마스코트 스냅샷 ID는 0 이상이어야 합니다");
        }
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("참가 제목은 필수입니다");
        }
        
        if (title.length() > 100) {
            throw new IllegalArgumentException("참가 제목은 100자 이하여야 합니다");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("참가 설명은 필수입니다");
        }
        
        if (description.length() > 500) {
            throw new IllegalArgumentException("참가 설명은 500자 이하여야 합니다");
        }
    }
}
