package com.solsolhey.ranking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 학교 정보 DTO
 */
@Getter
@AllArgsConstructor
public class SchoolInfo {
    private final Long id;     // 학교 ID
    private final String name; // 학교명
}
