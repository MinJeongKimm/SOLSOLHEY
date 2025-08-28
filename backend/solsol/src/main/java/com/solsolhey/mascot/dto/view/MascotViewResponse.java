package com.solsolhey.mascot.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotViewResponse {
    private OwnerSummary owner;
    private ViewerSummary viewer;
    private ViewMode viewMode;
    private Permissions permissions;
    private MascotSummary mascot;
}

