package com.solsolhey.mascot.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotSummary {
    private String name;
    private String type;
    private String equippedItem;
    private Integer level;
    private String backgroundId;
}

