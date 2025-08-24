package com.solsolhey.shop.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.shop.dto.GifticonResponse;
import com.solsolhey.shop.dto.ItemResponse;
import com.solsolhey.shop.dto.OrderRequest;
import com.solsolhey.shop.dto.OrderResponse;
import com.solsolhey.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shop API", description = "상점 관련 API")
public class ShopController {
    
    private final ShopService shopService;
    
    @Operation(summary = "상품 목록 조회", description = "타입별로 상품 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "상품 타입 (EQUIP, BACKGROUND)", example = "EQUIP")
            @RequestParam(required = false) String type) {
        
        try {
            Long userId = userDetails.getUserId();
            log.info("상품 목록 조회 API 호출 - 사용자 ID: {}, type: {}", userId, type);
            
            // 기존 로직을 새로운 메서드로 교체
            List<ItemResponse> items = shopService.getItemsWithOwnership(userId, type);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "상품 목록 조회가 완료되었습니다.");
            result.put("data", items);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "상품 목록 조회에 실패했습니다.");
        }
    }
    
    @Operation(summary = "주문 생성", description = "상품 또는 기프티콘을 구매합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주문 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody OrderRequest request) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("주문 생성 API 호출 - 사용자 ID: {}, type: {}", userId, request.getType());
            
            OrderResponse order = shopService.createOrder(userId, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", order.getMessage());
            result.put("data", order);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 주문 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("주문 처리 실패: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "주문 처리에 실패했습니다.");
        }
    }
    
    @Operation(summary = "기프티콘 목록 조회", description = "구매 가능한 기프티콘 목록을 조회합니다 (Mock)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/gifticons")
    public ResponseEntity<Map<String, Object>> getGifticons() {
        
        try {
            log.info("기프티콘 목록 조회 API 호출");
            
            List<GifticonResponse> gifticons = shopService.getGifticons();
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", "기프티콘 목록 조회가 완료되었습니다.");
            result.put("data", gifticons);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("기프티콘 목록 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "기프티콘 목록 조회에 실패했습니다.");
        }
    }
    
    @Operation(summary = "기프티콘 사용", description = "구매한 기프티콘을 사용합니다 (Mock)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "기프티콘 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/gifticons/{id}/redeem")
    public ResponseEntity<Map<String, Object>> redeemGifticon(
            @AuthenticationPrincipal Authentication authentication,
            @Parameter(description = "기프티콘 ID", example = "1")
            @PathVariable Long id) {
        
        try {
            Long userId = getUserId(authentication);
            log.info("기프티콘 사용 API 호출 - 사용자 ID: {}, 기프티콘 ID: {}", userId, id);
            
            String message = shopService.redeemGifticon(userId, id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("message", message);
            result.put("data", null);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 기프티콘 사용 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("기프티콘 사용 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "기프티콘 사용에 실패했습니다.");
        }
    }
    
    /**
     * Authentication에서 사용자 ID 추출
     * TODO: 실제 인증 구현에 맞게 수정 필요
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return 1L; // 기본 사용자 ID (테스트용)
        }
        
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.warn("사용자 ID 파싱 실패: {}", authentication.getName());
            return 1L; // 기본 사용자 ID (테스트용)
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

