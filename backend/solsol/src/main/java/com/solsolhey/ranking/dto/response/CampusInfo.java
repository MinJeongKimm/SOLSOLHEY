package com.solsolhey.ranking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 캠퍼스 정보 DTO
 */
@Getter
@AllArgsConstructor
public class CampusInfo {
    private final Long id;     // 캠퍼스 ID
    private final String name; // 캠퍼스명
}
