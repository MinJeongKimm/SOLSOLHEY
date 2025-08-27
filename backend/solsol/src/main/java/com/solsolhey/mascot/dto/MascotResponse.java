package com.solsolhey.mascot.dto;

import com.solsolhey.mascot.domain.Mascot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotResponse {
    
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private String equippedItem;
    private Integer exp;
    private Integer level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 최근 저장된 꾸미기 스냅샷 (필요 시 사용)
    private String snapshotImage;
    
    // Entity -> DTO 변환 팩토리 메서드
    public static MascotResponse from(Mascot mascot) {
        return MascotResponse.builder()
                .id(mascot.getId())
                .userId(mascot.getUserId())
                .name(mascot.getName())
                .type(mascot.getType())
                .equippedItem(mascot.getEquippedItem())
                .exp(mascot.getExp())
                .level(mascot.getLevel())
                .createdAt(mascot.getCreatedAt())
                .updatedAt(mascot.getUpdatedAt())
                .snapshotImage(mascot.getSnapshotImage())
                .build();
    }
}
