package com.solsolhey.shop.dto;

import com.solsolhey.shop.domain.UserGifticon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 보관함 기프티콘 상세 응답")
public class PurchasedGifticonDetailResponse {

    @Schema(description = "보관함 기프티콘 ID")
    private Long id;

    @Schema(description = "SKU")
    private String sku;

    @Schema(description = "기프티콘명")
    private String name;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "표시용 바코드/코드")
    private String barcode;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "만료일")
    private LocalDateTime expiresAt;

    public static PurchasedGifticonDetailResponse from(UserGifticon g) {
        return PurchasedGifticonDetailResponse.builder()
                .id(g.getId())
                .sku(g.getSku())
                .name(g.getName())
                .imageUrl(g.getImageUrl())
                .barcode(g.getBarcode())
                .status(g.getStatus().name())
                .expiresAt(g.getExpiresAt())
                .build();
    }
}

