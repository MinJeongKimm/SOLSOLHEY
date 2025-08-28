package com.solsolhey.mascot.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotCustomizationDto {
    @Builder.Default
    private String version = "v1";
    @NotNull
    private List<Item> equippedItems;

    // 선택: 저장 시 클라이언트에서 생성한 스냅샷(Data URL)
    // 조회 시에는 null로 내려보내도 무방
    private String snapshotImageDataUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        @NotNull
        private Long itemId;
        @NotNull
        private Position relativePosition; // 0..1 비율 좌표
        @NotNull
        private Double scale; // 배율 (예: 1.0)
        @NotNull
        private Double rotation; // 각도 0..360
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Position {
        @NotNull
        private Double x;
        @NotNull
        private Double y;
    }
}
