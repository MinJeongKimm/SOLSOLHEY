package com.solsolhey.solsol.dto.share;

import lombok.Builder;
import lombok.Getter;

/**
 * 공유 통계 응답 DTO
 */
@Getter
@Builder
public class ShareStatsResponse {

    private final Long totalShares; // 총 공유 수
    private final Long totalClicks; // 총 클릭 수
    private final Long totalImages; // 총 생성된 이미지 수
    private final Long totalDownloads; // 총 다운로드 수
    
    // 타입별 통계
    private final Long mascotShares;
    private final Long challengeShares;
    private final Long profileShares;
    private final Long achievementShares;
    
    public static ShareStatsResponse of(Long totalShares, Long totalClicks, Long totalImages, Long totalDownloads,
                                       Long mascotShares, Long challengeShares, Long profileShares, Long achievementShares) {
        return ShareStatsResponse.builder()
                .totalShares(totalShares)
                .totalClicks(totalClicks)
                .totalImages(totalImages)
                .totalDownloads(totalDownloads)
                .mascotShares(mascotShares)
                .challengeShares(challengeShares)
                .profileShares(profileShares)
                .achievementShares(achievementShares)
                .build();
    }
}
