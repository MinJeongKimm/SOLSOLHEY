package com.solsolhey.mascot.controller;

import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;
import com.solsolhey.mascot.exception.MascotAlreadyExistsException;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.service.MascotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mascot")
@RequiredArgsConstructor
@Slf4j
public class MascotController {
    
    private final MascotService mascotService;
    
    /**
     * 마스코트 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMascot(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody MascotCreateRequest request) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("마스코트 생성 API 호출 - 사용자 ID: {}", userId);
            
            MascotResponse response = mascotService.createMascot(userId, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "마스코트가 성공적으로 생성되었습니다.");
            result.put("data", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (MascotAlreadyExistsException e) {
            return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 생성 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "마스코트 생성에 실패했습니다.");
        }
    }
    
    /**
     * 마스코트 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMascot(
            @AuthenticationPrincipal Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("마스코트 조회 API 호출 - 사용자 ID: {}", userId);
            
            MascotResponse response = mascotService.getMascot(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "마스코트 조회가 완료되었습니다.");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "마스코트 조회에 실패했습니다.");
        }
    }
    
    /**
     * 마스코트 업데이트 (이름 변경, 경험치 증가)
     */
    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateMascot(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody MascotUpdateRequest request) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("마스코트 업데이트 API 호출 - 사용자 ID: {}", userId);
            
            MascotResponse response = mascotService.updateMascot(userId, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "마스코트가 성공적으로 업데이트되었습니다.");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 업데이트 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "마스코트 업데이트에 실패했습니다.");
        }
    }
    
    /**
     * 마스코트 아이템 장착
     */
    @PostMapping("/equip")
    public ResponseEntity<Map<String, Object>> equipMascot(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody MascotEquipRequest request) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("마스코트 아이템 장착 API 호출 - 사용자 ID: {}", userId);
            
            MascotResponse response = mascotService.equipMascot(userId, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "아이템이 성공적으로 장착되었습니다.");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 아이템 장착 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "아이템 장착에 실패했습니다.");
        }
    }
    
    /**
     * 마스코트 삭제
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteMascot(
            @AuthenticationPrincipal Authentication authentication) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("마스코트 삭제 API 호출 - 사용자 ID: {}", userId);
            
            mascotService.deleteMascot(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "마스코트가 성공적으로 삭제되었습니다.");
            result.put("data", null);
            
            return ResponseEntity.ok(result);
            
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 삭제 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "마스코트 삭제에 실패했습니다.");
        }
    }
    
    /**
     * Authentication에서 사용자 ID 추출
     * TODO: 실제 인증 구현에 맞게 수정 필요
     */
    private Long getUserId(Authentication authentication) {
        // 임시로 하드코딩된 사용자 ID 반환 (실제 구현 시 수정 필요)
        if (authentication == null || authentication.getName() == null) {
            return 1L; // 기본 사용자 ID
        }
        
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.warn("사용자 ID 파싱 실패: {}", authentication.getName());
            return 1L; // 기본 사용자 ID
        }
    }
    
    /**
     * 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("result", "ERROR");
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}
