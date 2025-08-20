package com.solsolhey.mascot.service;

import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;

public interface MascotService {
    
    /**
     * 마스코트 생성
     * @param userId 사용자 ID
     * @param request 마스코트 생성 요청
     * @return 생성된 마스코트 응답
     */
    MascotResponse createMascot(Long userId, MascotCreateRequest request);
    
    /**
     * 마스코트 조회
     * @param userId 사용자 ID
     * @return 마스코트 응답
     */
    MascotResponse getMascot(Long userId);
    
    /**
     * 마스코트 업데이트 (이름 변경, 경험치 증가)
     * @param userId 사용자 ID
     * @param request 마스코트 업데이트 요청
     * @return 업데이트된 마스코트 응답
     */
    MascotResponse updateMascot(Long userId, MascotUpdateRequest request);
    
    /**
     * 마스코트 아이템 장착
     * @param userId 사용자 ID
     * @param request 아이템 장착 요청
     * @return 업데이트된 마스코트 응답
     */
    MascotResponse equipMascot(Long userId, MascotEquipRequest request);
    
    /**
     * 마스코트 삭제
     * @param userId 사용자 ID
     */
    void deleteMascot(Long userId);
}
