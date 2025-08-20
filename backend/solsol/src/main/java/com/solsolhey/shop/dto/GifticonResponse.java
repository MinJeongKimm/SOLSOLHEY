package com.solsolhey.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "기프티콘 응답")
public class GifticonResponse {
    
    @Schema(description = "기프티콘 ID", example = "1")
    private Long id;
    
    @Schema(description = "기프티콘명", example = "스타벅스 아메리카노")
    private String name;
    
    @Schema(description = "이미지 URL", example = "https://example.com/starbucks.png")
    private String imageUrl;
    
    @Schema(description = "가격", example = "4500")
    private Integer price;
    
    @Schema(description = "설명", example = "진한 에스프레소에 물을 더한 깔끔한 맛의 아메리카노")
    private String description;
    
    @Schema(description = "SKU", example = "STARBUCKS_AMERICANO")
    private String sku;
}

