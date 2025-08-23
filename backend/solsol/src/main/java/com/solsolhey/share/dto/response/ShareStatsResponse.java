package com.solsolhey.share.dto.response;

import lombok.Builder;

/**
 * 공유 통계 응답 DTO
 */
@Builder
public record ShareStatsResponse(
    Long totalShareLinks,
    Long totalShareImages,
    Long totalLinkClicks,
    Long totalImageShares,
    Long activeLinks,
    Long publicImages,
    Long expiredLinks,
    Double averageClicksPerLink,
    Double averageSharesPerImage
) {
    public static ShareStatsResponse of(Long totalShareLinks, Long totalShareImages,
                                       Long totalLinkClicks, Long totalImageShares,
                                       Long activeLinks, Long publicImages,
                                       Long expiredLinks) {
        Double averageClicksPerLink = totalShareLinks > 0 ? 
                (double) totalLinkClicks / totalShareLinks : 0.0;
        Double averageSharesPerImage = totalShareImages > 0 ? 
                (double) totalImageShares / totalShareImages : 0.0;

        return ShareStatsResponse.builder()
                .totalShareLinks(totalShareLinks)
                .totalShareImages(totalShareImages)
                .totalLinkClicks(totalLinkClicks)
                .totalImageShares(totalImageShares)
                .activeLinks(activeLinks)
                .publicImages(publicImages)
                .expiredLinks(expiredLinks)
                .averageClicksPerLink(Math.round(averageClicksPerLink * 100.0) / 100.0)
                .averageSharesPerImage(Math.round(averageSharesPerImage * 100.0) / 100.0)
                .build();
    }
}
