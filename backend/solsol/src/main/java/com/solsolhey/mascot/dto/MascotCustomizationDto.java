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

