package com.solsolhey.shop.dto;

import com.solsolhey.shop.domain.Item;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 응답")
public class ItemResponse {
    
    @Schema(description = "상품 ID", example = "1")
    private Long id;
    
    @Schema(description = "상품명", example = "멋진 모자")
    private String name;
    
    @Schema(description = "상품 설명", example = "마스코트를 더욱 멋지게 만들어주는 모자")
    private String description;
    
    @Schema(description = "가격", example = "100")
    private Integer price;
    
    @Schema(description = "상품 타입", example = "EQUIP")
    private String type;
    
    @Schema(description = "이미지 URL", example = "https://example.com/hat.png")
    private String imageUrl;
    
    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .type(item.getType().name())
                .imageUrl(item.getImageUrl())
                .build();
    }
}

