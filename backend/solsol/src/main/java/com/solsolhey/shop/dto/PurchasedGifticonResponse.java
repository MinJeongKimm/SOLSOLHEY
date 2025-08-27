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
@Schema(description = "사용자 보관함 기프티콘 목록 응답")
public class PurchasedGifticonResponse {

    @Schema(description = "보관함 기프티콘 ID", example = "1")
    private Long id;

    @Schema(description = "기프티콘명", example = "스타벅스 아메리카노")
    private String name;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "상태", example = "ACTIVE")
    private String status;

    @Schema(description = "만료일")
    private LocalDateTime expiresAt;

    public static PurchasedGifticonResponse from(UserGifticon g) {
        return PurchasedGifticonResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .imageUrl(g.getImageUrl())
                .status(g.getStatus().name())
                .expiresAt(g.getExpiresAt())
                .build();
    }
}

