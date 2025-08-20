package com.solsolhey.solsol.controller;

import com.solsolhey.solsol.auth.dto.CustomUserDetails;
import com.solsolhey.solsol.common.response.ApiResponse;
import com.solsolhey.solsol.dto.friend.*;
import com.solsolhey.solsol.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 친구 관리 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendController {

    private final FriendService friendService;

    /**
     * 친구 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FriendResponse>> addFriend(
            @Valid @RequestBody FriendAddRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 추가 요청: userId={}", userDetails.getUserId());

        // Validation 에러 처리
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("요청 데이터가 올바르지 않습니다: ");
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, errorMessage.toString()));
        }

        try {
            FriendResponse response = friendService.addFriend(userDetails.getUser(), request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("친구 추가가 완료되었습니다.", response));
                    
        } catch (Exception e) {
            log.error("친구 추가 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 친구 삭제
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 삭제 요청: userId={}, friendId={}", userDetails.getUserId(), friendId);

        try {
            friendService.removeFriend(userDetails.getUser(), friendId);
            
            return ResponseEntity.ok(ApiResponse.success("친구가 삭제되었습니다.", "삭제 완료"));
                    
        } catch (Exception e) {
            log.error("친구 삭제 실패: userId={}, friendId={}", userDetails.getUserId(), friendId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 친구 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FriendListResponse>> getFriendList(
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 목록 조회: userId={}, size={}", userDetails.getUserId(), size);

        try {
            FriendListResponse response = friendService.getFriendList(userDetails.getUser(), size);
            
            return ResponseEntity.ok(ApiResponse.success("친구 목록을 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("친구 목록 조회 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "친구 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 친구 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<FriendSearchResponse>> searchFriends(
            @RequestParam String q,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 검색: userId={}, query={}", userDetails.getUserId(), q);

        try {
            FriendSearchResponse response = friendService.searchFriends(userDetails.getUser(), q);
            
            return ResponseEntity.ok(ApiResponse.success("친구 검색이 완료되었습니다.", response));
                    
        } catch (Exception e) {
            log.error("친구 검색 실패: userId={}, query={}", userDetails.getUserId(), q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "친구 검색에 실패했습니다."));
        }
    }

    /**
     * 사용자명으로 친구 조회
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<ApiResponse<FriendResponse>> getFriendByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("사용자명으로 친구 조회: userId={}, username={}", userDetails.getUserId(), username);

        try {
            FriendResponse response = friendService.getFriendByUsername(userDetails.getUser(), username);
            
            return ResponseEntity.ok(ApiResponse.success("사용자 정보를 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("사용자 조회 실패: userId={}, username={}", userDetails.getUserId(), username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        }
    }

    /**
     * 친구 상호작용 (좋아요, 방문, 응원)
     */
    @PostMapping("/interact")
    public ResponseEntity<ApiResponse<FriendInteractionResponse>> interactWithFriend(
            @Valid @RequestBody FriendInteractionRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 상호작용: userId={}, targetUserId={}, type={}", 
                userDetails.getUserId(), request.getTargetUserId(), request.getInteractionType());

        // Validation 에러 처리
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("요청 데이터가 올바르지 않습니다: ");
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, errorMessage.toString()));
        }

        try {
            FriendInteractionResponse response = friendService.interactWithFriend(userDetails.getUser(), request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, response.getMessage()));
            }
                    
        } catch (Exception e) {
            log.error("친구 상호작용 실패: userId={}, targetUserId={}", 
                    userDetails.getUserId(), request.getTargetUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "상호작용 처리에 실패했습니다."));
        }
    }

    /**
     * 친구 초대 코드 생성
     */
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateInvitationCode(
            @RequestParam(required = false) Integer expiresInHours,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 초대 코드 생성: userId={}, expiresInHours={}", userDetails.getUserId(), expiresInHours);

        try {
            String invitationCode = friendService.generateInvitationCode(userDetails.getUser(), expiresInHours);
            
            Map<String, String> response = new HashMap<>();
            response.put("invitationCode", invitationCode);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("초대 코드가 생성되었습니다.", response));
                    
        } catch (Exception e) {
            log.error("초대 코드 생성 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "초대 코드 생성에 실패했습니다."));
        }
    }

    /**
     * 친구 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FriendStatsResponse>> getFriendStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("친구 통계 조회: userId={}", userDetails.getUserId());

        try {
            FriendStatsResponse response = friendService.getFriendStats(userDetails.getUser());
            
            return ResponseEntity.ok(ApiResponse.success("친구 통계를 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("친구 통계 조회 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "친구 통계 조회에 실패했습니다."));
        }
    }
}