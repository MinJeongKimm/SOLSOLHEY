package com.solsolhey.ranking.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 랭킹 조회 응답 DTO (교내/전국 공통)
 */
@Getter
@Builder
public class RankingResponse {

    private final Integer total;                    // 전체 엔트리 개수
    private final Integer page;                     // 현재 페이지 번호
    private final Integer size;                     // 페이지 크기
    private final String period;                    // 집계 기간
    private final String sort;                      // 정렬 기준
    private final CampusInfo campus;                // 캠퍼스 정보 (교내 랭킹용)
    private final FilterInfo filters;               // 적용된 필터 요약 (전국 랭킹용)
    private final List<RankingEntryResponse> entries; // 랭킹 엔트리 목록

    /**
     * 교내 랭킹 응답 생성
     */
    public static RankingResponse forCampus(int total, int page, int size, String period, String sort,
                                          CampusInfo campus, List<RankingEntryResponse> entries) {
        return RankingResponse.builder()
                .total(total)
                .page(page)
                .size(size)
                .period(period)
                .sort(sort)
                .campus(campus)
                .entries(entries)
                .build();
    }

    /**
     * 전국 랭킹 응답 생성
     */
    public static RankingResponse forNational(int total, int page, int size, String period, String sort,
                                            FilterInfo filters, List<RankingEntryResponse> entries) {
        return RankingResponse.builder()
                .total(total)
                .page(page)
                .size(size)
                .period(period)
                .sort(sort)
                .filters(filters)
                .entries(entries)
                .build();
    }
}
