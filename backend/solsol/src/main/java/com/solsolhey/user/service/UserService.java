package com.solsolhey.user.service;

import com.solsolhey.user.dto.response.UserResponse;
import com.solsolhey.user.dto.request.UserUpdateRequest;

import java.util.List;

/**
 * 사용자 서비스 인터페이스
 */
public interface UserService {
    
    /**
     * 사용자 정보 조회
     */
    UserResponse getUserById(Long userId);
    
    /**
     * 모든 사용자 조회
     */
    List<UserResponse> getAllUsers();
    
    /**
     * 닉네임으로 사용자 검색
     */
    List<UserResponse> searchUsersByNickname(String nickname);
    
    /**
     * 사용자 정보 업데이트
     */
    UserResponse updateUser(Long userId, UserUpdateRequest request);
    
    /**
     * 사용자 포인트 추가
     */
    UserResponse addPoints(Long userId, Integer points);
    
    /**
     * 사용자 포인트 차감
     */
    UserResponse deductPoints(Long userId, Integer points);
    
    /**
     * 사용자 계정 비활성화
     */
    void deactivateUser(Long userId);
    
    /**
     * 사용자 계정 활성화
     */
    void activateUser(Long userId);
}
