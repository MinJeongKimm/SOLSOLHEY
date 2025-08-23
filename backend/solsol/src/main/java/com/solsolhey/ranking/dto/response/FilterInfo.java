package com.solsolhey.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 적용된 필터 정보 DTO (전국 랭킹용)
 */
@Getter
@Builder
public class FilterInfo {
    private final String region;   // 지역
    private final Long schoolId;   // 학교 ID
    
    public static FilterInfo of(String region, Long schoolId) {
        return FilterInfo.builder()
                .region(region)
                .schoolId(schoolId)
                .build();
    }
}
