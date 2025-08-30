package com.solsolhey.ranking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 전국 랭킹 조회 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class NationalRankingRequest {

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
     * 지역 필터 (예: seoul, busan)
     */
    private String region;

    /**
     * 특정 학교만 필터링
     */
    private Long schoolId;

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

    public NationalRankingRequest(String sort, String period, String region, 
                                 Long schoolId, Integer page, Integer size) {
        this.sort = sort != null ? sort : "votes_desc";
        this.period = period != null ? period : "weekly";
        this.region = region;
        this.schoolId = schoolId;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
    }
}
