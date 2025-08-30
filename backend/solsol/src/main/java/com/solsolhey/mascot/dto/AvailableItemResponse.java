package com.solsolhey.mascot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 꾸미기용 보유 아이템 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용 가능한 아이템 응답")
public class AvailableItemResponse {
    
    @Schema(description = "아이템 ID", example = "1001")
    private Long itemId;
    
    @Schema(description = "아이템명", example = "토끼 귀")
    private String itemName;
    
    @Schema(description = "아이템 타입", example = "EQUIP")
    private String itemType;
    
    @Schema(description = "이미지 URL", example = "/items/item_head_bunny_ears.png")
    private String imageUrl;
    
    @Schema(description = "보유 수량", example = "1")
    private Integer quantity;
    
    @Schema(description = "장착 여부", example = "false")
    private Boolean isEquipped;
    
    @Schema(description = "구매일", example = "2024-12-19T10:00:00")
    private LocalDateTime purchasedAt;
}
