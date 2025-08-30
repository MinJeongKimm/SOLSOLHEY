package com.solsolhey.mascot.service;

import com.solsolhey.mascot.dto.view.MascotViewResponse;

public interface MascotViewService {
    MascotViewResponse getView(Long viewerId, Long ownerId);
    
    /**
     * 공개 마스코트 조회 (로그인하지 않은 사용자용)
     */
    MascotViewResponse getPublicView(Long ownerId);
}

