package com.solsolhey.user.controller;

import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.user.dto.response.UserResponse;
import com.solsolhey.user.dto.request.UserUpdateRequest;
import com.solsolhey.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 사용자 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@Tag(name = "User API", description = "사용자 관리 관련 API")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 사용자 정보 조회
     */
    @GetMapping("/{userId}")
    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        try {
            UserResponse user = userService.getUserById(userId);
            ApiResponse<UserResponse> response = ApiResponse.success("사용자 정보 조회 완료", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 모든 사용자 조회
     */
    @GetMapping
    @Operation(summary = "전체 사용자 조회", description = "모든 사용자의 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            ApiResponse<List<UserResponse>> response = ApiResponse.success("전체 사용자 조회 완료", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("전체 사용자 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 닉네임으로 사용자 검색
     */
    @GetMapping("/search")
    @Operation(summary = "사용자 검색", description = "닉네임으로 사용자를 검색합니다")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String nickname) {
        try {
            List<UserResponse> users = userService.searchUsersByNickname(nickname);
            ApiResponse<List<UserResponse>> response = ApiResponse.success("사용자 검색 완료", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 검색 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 정보 업데이트
     */
    @PutMapping("/{userId}")
    @Operation(summary = "사용자 정보 업데이트", description = "사용자의 정보를 수정합니다")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            UserResponse updatedUser = userService.updateUser(userId, request);
            ApiResponse<UserResponse> response = ApiResponse.success("사용자 정보 업데이트 완료", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 포인트 추가
     */
    @PostMapping("/{userId}/points/add")
    @Operation(summary = "포인트 추가", description = "사용자에게 포인트를 추가합니다")
    public ResponseEntity<ApiResponse<UserResponse>> addPoints(
            @PathVariable Long userId,
            @RequestParam Integer points) {
        try {
            UserResponse updatedUser = userService.addPoints(userId, points);
            ApiResponse<UserResponse> response = ApiResponse.success("포인트 추가 완료", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포인트 추가 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 포인트 차감
     */
    @PostMapping("/{userId}/points/deduct")
    @Operation(summary = "포인트 차감", description = "사용자의 포인트를 차감합니다")
    public ResponseEntity<ApiResponse<UserResponse>> deductPoints(
            @PathVariable Long userId,
            @RequestParam Integer points) {
        try {
            UserResponse updatedUser = userService.deductPoints(userId, points);
            ApiResponse<UserResponse> response = ApiResponse.success("포인트 차감 완료", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포인트 차감 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 계정 비활성화
     */
    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "계정 비활성화", description = "사용자 계정을 비활성화합니다")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            ApiResponse<Void> response = ApiResponse.success("계정 비활성화 완료", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("계정 비활성화 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 계정 활성화
     */
    @PostMapping("/{userId}/activate")
    @Operation(summary = "계정 활성화", description = "비활성화된 사용자 계정을 활성화합니다")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long userId) {
        try {
            userService.activateUser(userId);
            ApiResponse<Void> response = ApiResponse.success("계정 활성화 완료", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("계정 활성화 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
