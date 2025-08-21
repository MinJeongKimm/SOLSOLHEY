package com.solsolhey.solsol.dto.ranking;

import lombok.Getter;
import lombok.Setter;

/**
 * 투표 요청 DTO
 */
@Getter
@Setter
public class VoteRequestDto {

    private Long entryId;
    private String contestType;
}
