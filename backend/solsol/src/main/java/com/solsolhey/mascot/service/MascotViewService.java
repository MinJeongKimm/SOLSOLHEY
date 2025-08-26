package com.solsolhey.mascot.service;

import com.solsolhey.mascot.dto.view.MascotViewResponse;

public interface MascotViewService {
    MascotViewResponse getView(Long viewerId, Long ownerId);
}

