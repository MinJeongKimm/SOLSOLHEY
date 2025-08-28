package com.solsolhey.ranking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 교내 랭킹 조회 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CampusRankingRequest {

    /**
     * 조회 대상 캠퍼스 ID (미지정 시 토큰의 기본 소속 캠퍼스)
     */
    private Long campusId;

    /**
     * 정렬 기준 (votes_desc, trending, newest)
     */
    @Pattern(regexp = "^(votes_desc|trending|newest)$", 
             message = "정렬 기준은 votes_desc, trending, newest 중 하나여야 합니다.")
    private String sort = "votes_desc";

    /**
     * 집계 기간 (daily, weekly, monthly, all)
     */
    @Pattern(regexp = "^(daily|weekly|monthly|all)$", 
             message = "집계 기간은 daily, weekly, monthly, all 중 하나여야 합니다.")
    private String period = "weekly";

    /**
     * 페이지 번호 (0부터 시작)
     */
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page = 0;

    /**
     * 페이지 크기 (최대 100)
     */
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    private Integer size = 20;

    public CampusRankingRequest(Long campusId, String sort, String period, Integer page, Integer size) {
        this.campusId = campusId;
        this.sort = sort != null ? sort : "votes_desc";
        this.period = period != null ? period : "weekly";
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
    }
}
