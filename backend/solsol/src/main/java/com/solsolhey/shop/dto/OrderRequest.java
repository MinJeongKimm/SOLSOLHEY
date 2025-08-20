package com.solsolhey.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 요청")
public class OrderRequest {
    
    @NotBlank(message = "주문 타입은 필수입니다")
    @Schema(description = "주문 타입", example = "ITEM", allowableValues = {"ITEM", "GIFTICON"})
    private String type;
    
    @Schema(description = "상품 ID (ITEM 타입인 경우)", example = "1")
    private Long itemId;
    
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    @Schema(description = "수량 (ITEM 타입인 경우)", example = "1", defaultValue = "1")
    private Integer quantity = 1;
    
    @Schema(description = "기프티콘 SKU (GIFTICON 타입인 경우)", example = "STARBUCKS_AMERICANO")
    private String sku;
}

