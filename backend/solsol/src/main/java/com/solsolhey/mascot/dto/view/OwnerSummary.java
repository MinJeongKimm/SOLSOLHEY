package com.solsolhey.mascot.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSummary {
    private Long id;
    private String nickname;
    private Integer level;
}

