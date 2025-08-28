package com.solsolhey.mascot.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.mascot.dto.ApplyBackgroundRequest;
import com.solsolhey.mascot.dto.ApplyBackgroundResponse;
import com.solsolhey.mascot.dto.AvailableItemResponse;
import com.solsolhey.mascot.dto.BackgroundResponse;
import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotBackgroundUpdateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotCustomizationDto;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;
import com.solsolhey.mascot.dto.UserBackgroundResponse;
import com.solsolhey.mascot.exception.MascotAlreadyExistsException;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.service.MascotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MascotCreateRequest request) {
        
        try {
            Long userId = userDetails.getUserId();
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
     * 마스코트 커스터마이징 조회
     */
    @GetMapping("/customization")
    public ResponseEntity<Map<String, Object>> getCustomization(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUserId();
            log.info("마스코트 커스터마이징 조회 API 호출 - 사용자 ID: {}", userId);

            MascotCustomizationDto dto = mascotService.getCustomization(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "커스터마이징 조회가 완료되었습니다.");
            result.put("data", dto);
            return ResponseEntity.ok(result);
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 커스터마이징 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "커스터마이징 조회에 실패했습니다.");
        }
    }

    /**
     * 배경 커스터마이징(색상/패턴) 저장
     */
    @PutMapping("/background")
    public ResponseEntity<Map<String, Object>> updateBackgroundCustomization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MascotBackgroundUpdateRequest request) {
        try {
            Long userId = userDetails.getUserId();
            log.info("배경 커스터마이징 저장 - 사용자 ID: {}, color: {}, pattern: {}",
                    userId, request.getBackgroundColor(), request.getPatternType());

            MascotResponse updated = mascotService.updateBackgroundCustomization(
                    userId,
                    request.getBackgroundColor(),
                    request.getPatternType() == null ? "none" : request.getPatternType()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "배경 설정이 저장되었습니다.");
            result.put("data", updated);
            return ResponseEntity.ok(result);
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("배경 커스터마이징 저장 실패", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "배경 설정 저장에 실패했습니다.");
        }
    }

    /**
     * 마스코트 커스터마이징 저장
     */
    @PutMapping("/customization")
    public ResponseEntity<Map<String, Object>> saveCustomization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MascotCustomizationDto request) {
        try {
            Long userId = userDetails.getUserId();
            log.info("마스코트 커스터마이징 저장 API 호출 - 사용자 ID: {}", userId);

            MascotCustomizationDto saved = mascotService.saveCustomization(userId, request);
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "커스터마이징이 저장되었습니다.");
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (MascotNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("마스코트 커스터마이징 저장 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "커스터마이징 저장에 실패했습니다.");
        }
    }
    
    /**
     * 마스코트 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMascot(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            Long userId = userDetails.getUserId();
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
     * 최근 스냅샷 목록 조회 (최대 20개)
     */
    @GetMapping("/snapshots")
    public ResponseEntity<Map<String, Object>> getSnapshots(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUserId();
            log.info("마스코트 스냅샷 조회 API 호출 - 사용자 ID: {}", userId);

            var snapshots = mascotService.getSnapshotHistory(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "스냅샷 조회가 완료되었습니다.");
            result.put("data", snapshots);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("스냅샷 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "스냅샷 조회에 실패했습니다.");
        }
    }
    
    /**
     * 마스코트 업데이트 (이름 변경, 경험치 증가)
     */
    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateMascot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MascotUpdateRequest request) {
        
        try {
            Long userId = userDetails.getUserId();
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MascotEquipRequest request) {
        
        try {
            if (userDetails == null) return createErrorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            Long userId = userDetails.getUserId();
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
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            if (userDetails == null) return createErrorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            Long userId = userDetails.getUserId();
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
    

    // === 배경 관련 엔드포인트 ===
    
    /**
     * 배경 목록 조회
     */
    @GetMapping("/backgrounds")
    public ResponseEntity<List<BackgroundResponse>> getBackgrounds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "true") Boolean enabled,
            @RequestParam(required = false) String tags) {
        
        try {
            Long userId = userDetails.getUserId();
            log.info("배경 목록 조회 API 호출 - 사용자 ID: {}, enabled: {}, tags: {}", userId, enabled, tags);
            
            List<String> tagList = null;
            if (tags != null && !tags.trim().isEmpty()) {
                tagList = Arrays.asList(tags.split(","))
                        .stream()
                        .map(String::trim)
                        .toList();
            }
            
            List<BackgroundResponse> response = mascotService.getBackgrounds(enabled, tagList, userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("배경 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 사용자 보유 배경 목록 조회
     */
    @GetMapping("/backgrounds/owned")
    public ResponseEntity<List<UserBackgroundResponse>> getUserOwnedBackgrounds(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            Long userId = userDetails.getUserId();
            log.info("사용자 보유 배경 목록 조회 API 호출 - 사용자 ID: {}", userId);
            
            List<UserBackgroundResponse> response = mascotService.getUserOwnedBackgrounds(userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 보유 배경 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 마스코트 배경 적용
     */
    @PutMapping("/mascots/{mascotId}/background")
    public ResponseEntity<ApplyBackgroundResponse> applyBackground(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mascotId,
            @Valid @RequestBody ApplyBackgroundRequest request) {
        
        try {
            Long userId = userDetails.getUserId();
            log.info("마스코트 배경 적용 API 호출 - 사용자 ID: {}, 마스코트 ID: {}, 배경 ID: {}", 
                     userId, mascotId, request.getBackgroundId());
            
            ApplyBackgroundResponse response = mascotService.applyBackground(mascotId, request, userId);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("마스코트 배경 적용 실패: {}", e.getMessage());
            
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("보유하고 있지 않습니다")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("마스코트 배경 적용 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 마스코트 배경 해제 (기본 배경 적용)
     */
    @DeleteMapping("/mascots/{mascotId}/background")
    public ResponseEntity<ApplyBackgroundResponse> resetBackground(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mascotId) {
        
        try {
            if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            Long userId = userDetails.getUserId();
            log.info("마스코트 배경 해제 API 호출 - 사용자 ID: {}, 마스코트 ID: {}", userId, mascotId);
            
            ApplyBackgroundResponse response = mascotService.resetBackground(mascotId, userId);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("마스코트 배경 해제 실패: {}", e.getMessage());
            
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("마스코트 배경 해제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자가 보유한 아이템 목록 조회 (꾸미기용)
     */
    @GetMapping("/available-items")
    public ResponseEntity<Map<String, Object>> getAvailableItems(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            if (userDetails == null) return createErrorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            Long userId = userDetails.getUserId();
            log.info("사용 가능한 아이템 조회 API 호출 - 사용자 ID: {}", userId);
            
            List<AvailableItemResponse> items = mascotService.getAvailableItems(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "사용 가능한 아이템 조회가 완료되었습니다.");
            result.put("data", items);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("사용 가능한 아이템 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "사용 가능한 아이템 조회에 실패했습니다.");
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
