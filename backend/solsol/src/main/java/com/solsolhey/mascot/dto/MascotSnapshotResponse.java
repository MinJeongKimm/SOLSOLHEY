package com.solsolhey.mascot.dto;

import java.time.LocalDateTime;

import com.solsolhey.mascot.domain.MascotSnapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotSnapshotResponse {
    private Long id;
    private Long userId;
    private Long mascotId;
    private String imageUrl; // 정적 URL
    private LocalDateTime createdAt;

    public static MascotSnapshotResponse from(MascotSnapshot s) {
        return MascotSnapshotResponse.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .mascotId(s.getMascotId())
                .imageUrl(s.getImageUrl())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
