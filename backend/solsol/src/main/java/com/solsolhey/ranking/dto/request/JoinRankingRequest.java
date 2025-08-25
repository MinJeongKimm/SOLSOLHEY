package com.solsolhey.ranking.dto.request;

import com.solsolhey.ranking.entity.ContestEntry;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinRankingRequest {
    @NotNull
    private Long mascotId;

    @NotNull
    private ContestEntry.ContestType contestType;
    
    private String thumbnailUrl;
}
