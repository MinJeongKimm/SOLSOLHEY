package com.solsolhey.mascot.service;

import java.util.List;

import com.solsolhey.mascot.dto.ApplyBackgroundRequest;
import com.solsolhey.mascot.dto.ApplyBackgroundResponse;
import com.solsolhey.mascot.dto.BackgroundResponse;
import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;
import com.solsolhey.mascot.dto.UserBackgroundResponse;

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
    
    // === 배경 관련 메서드 ===
    
    /**
     * 배경 목록 조회
     * @param enabled 활성화 여부 (null이면 전체)
     * @param tags 태그 목록 (null이면 태그 필터 없음)
     * @param userId 사용자 ID (보유 여부 확인용)
     * @return 배경 목록
     */
    List<BackgroundResponse> getBackgrounds(Boolean enabled, List<String> tags, Long userId);
    
    /**
     * 사용자 보유 배경 목록 조회
     * @param userId 사용자 ID
     * @return 사용자 보유 배경 목록
     */
    List<UserBackgroundResponse> getUserOwnedBackgrounds(Long userId);
    
    /**
     * 마스코트 배경 적용
     * @param mascotId 마스코트 ID
     * @param request 배경 적용 요청
     * @param userId 사용자 ID
     * @return 배경 적용 응답
     */
    ApplyBackgroundResponse applyBackground(Long mascotId, ApplyBackgroundRequest request, Long userId);
    
    /**
     * 마스코트 배경 해제 (기본 배경 적용)
     * @param mascotId 마스코트 ID
     * @param userId 사용자 ID
     * @return 배경 적용 응답
     */
    ApplyBackgroundResponse resetBackground(Long mascotId, Long userId);
}
