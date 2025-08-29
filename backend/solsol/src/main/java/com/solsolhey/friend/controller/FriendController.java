package com.solsolhey.friend.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.friend.dto.request.FriendAddRequest;
import com.solsolhey.friend.dto.request.FriendInteractionRequest;
import com.solsolhey.friend.dto.response.*;
import com.solsolhey.friend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;

/**
 * 친구 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friend API", description = "친구 관리 관련 API")
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;

    /**
     * 친구 요청 보내기
     */
    @PostMapping("/requests")
    @Operation(summary = "친구 요청 보내기", description = "다른 사용자에게 친구 요청을 보냅니다")
    public ResponseEntity<ApiResponse<FriendResponse>> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FriendAddRequest request) {
        try {
            log.info("친구 요청 API 호출: userId={}", userDetails.getUserId());
            FriendResponse response = friendService.sendFriendRequest(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("친구 요청을 보냈습니다.", response));
        } catch (Exception e) {
            log.error("친구 요청 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 요청 수락
     */
    @PutMapping("/requests/{friendId}/accept")
    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락합니다")
    public ResponseEntity<ApiResponse<FriendResponse>> acceptFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long friendId) {
        try {
            log.info("친구 요청 수락 API 호출: userId={}, friendId={}", userDetails.getUserId(), friendId);
            FriendResponse response = friendService.acceptFriendRequest(userDetails.getUser(), friendId);
            return ResponseEntity.ok(ApiResponse.success("친구 요청을 수락했습니다.", response));
        } catch (Exception e) {
            log.error("친구 요청 수락 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 요청 거절
     */
    @PutMapping("/requests/{friendId}/reject")
    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절합니다")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long friendId) {
        try {
            log.info("친구 요청 거절 API 호출: userId={}, friendId={}", userDetails.getUserId(), friendId);
            friendService.rejectFriendRequest(userDetails.getUser(), friendId);
            return ResponseEntity.ok(ApiResponse.success("친구 요청을 거절했습니다.", null));
        } catch (Exception e) {
            log.error("친구 요청 거절 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 삭제
     */
    @DeleteMapping("/{friendId}")
    @Operation(summary = "친구 삭제", description = "기존 친구 관계를 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long friendId) {
        try {
            log.info("친구 삭제 API 호출: userId={}, friendId={}", userDetails.getUserId(), friendId);
            friendService.removeFriend(userDetails.getUser(), friendId);
            return ResponseEntity.ok(ApiResponse.success("친구를 삭제했습니다.", null));
        } catch (Exception e) {
            log.error("친구 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 목록 조회
     */
    @GetMapping
    @Operation(summary = "친구 목록 조회", description = "현재 사용자의 친구 목록을 페이지네이션으로 조회합니다")
    public ResponseEntity<ApiResponse<Page<FriendResponse>>> getFriends(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("친구 목록 조회 API 호출: userId={}", userDetails.getUserId());
            Page<FriendResponse> friends = friendService.getFriends(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("친구 목록을 조회했습니다.", friends));
        } catch (Exception e) {
            log.error("친구 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 받은 친구 요청 목록 조회
     */
    @GetMapping("/requests/received")
    @Operation(summary = "받은 친구 요청 목록", description = "다른 사용자로부터 받은 친구 요청 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<FriendResponse>>> getPendingFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("받은 친구 요청 목록 API 호출: userId={}", userDetails.getUserId());
            Page<FriendResponse> requests = friendService.getPendingFriendRequests(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("받은 친구 요청 목록을 조회했습니다.", requests));
        } catch (Exception e) {
            log.error("받은 친구 요청 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 보낸 친구 요청 목록 조회
     */
    @GetMapping("/requests/sent")
    @Operation(summary = "보낸 친구 요청 목록", description = "현재 사용자가 보낸 친구 요청 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<FriendResponse>>> getSentFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("보낸 친구 요청 목록 API 호출: userId={}", userDetails.getUserId());
            Page<FriendResponse> requests = friendService.getSentFriendRequests(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("보낸 친구 요청 목록을 조회했습니다.", requests));
        } catch (Exception e) {
            log.error("보낸 친구 요청 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 검색
     */
    @GetMapping("/search")
    @Operation(summary = "친구 검색", description = "키워드로 친구를 검색합니다")
    public ResponseEntity<ApiResponse<List<FriendSearchResponse>>> searchFriends(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword) {
        try {
            log.info("친구 검색 API 호출: userId={}, keyword={}", userDetails.getUserId(), keyword);
            List<FriendSearchResponse> results = friendService.searchFriends(userDetails.getUser(), keyword);
            return ResponseEntity.ok(ApiResponse.success("친구 검색을 완료했습니다.", results));
        } catch (Exception e) {
            log.error("친구 검색 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 통계 조회
     */
    @GetMapping("/stats")
    @Operation(summary = "친구 통계 조회", description = "친구 관련 통계 정보를 조회합니다")
    public ResponseEntity<ApiResponse<FriendStatsResponse>> getFriendStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("친구 통계 API 호출: userId={}", userDetails.getUserId());
            FriendStatsResponse stats = friendService.getFriendStats(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("친구 통계를 조회했습니다.", stats));
        } catch (Exception e) {
            log.error("친구 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 상호작용 보내기
     */
    @PostMapping("/interactions")
    @Operation(summary = "상호작용 보내기", description = "친구에게 상호작용(좋아요, 응원 등)을 보냅니다")
    public ResponseEntity<ApiResponse<FriendInteractionResponse>> sendInteraction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FriendInteractionRequest request) {
        try {
            log.info("상호작용 보내기 API 호출: userId={}", userDetails.getUserId());
            FriendInteractionResponse response = friendService.sendInteraction(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("상호작용을 보냈습니다.", response));
        } catch (Exception e) {
            log.error("상호작용 보내기 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 받은 상호작용 목록 조회
     */
    @GetMapping("/interactions")
    @Operation(summary = "받은 상호작용 목록", description = "다른 사용자로부터 받은 상호작용 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<FriendInteractionResponse>>> getReceivedInteractions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("받은 상호작용 목록 API 호출: userId={}", userDetails.getUserId());
            Page<FriendInteractionResponse> interactions = friendService.getReceivedInteractions(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("받은 상호작용 목록을 조회했습니다.", interactions));
        } catch (Exception e) {
            log.error("받은 상호작용 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 읽지 않은 상호작용 수 조회
     */
    @GetMapping("/interactions/unread-count")
    @Operation(summary = "읽지 않은 상호작용 수", description = "읽지 않은 상호작용의 개수를 조회합니다")
    public ResponseEntity<ApiResponse<Long>> getUnreadInteractionCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("읽지 않은 상호작용 수 API 호출: userId={}", userDetails.getUserId());
            Long count = friendService.getUnreadInteractionCount(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("읽지 않은 상호작용 수를 조회했습니다.", count));
        } catch (Exception e) {
            log.error("읽지 않은 상호작용 수 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 상호작용 읽음 처리
     */
    @PutMapping("/interactions/{interactionId}/read")
    @Operation(summary = "상호작용 읽음 처리", description = "특정 상호작용을 읽음 처리합니다")
    public ResponseEntity<ApiResponse<Void>> markInteractionAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interactionId) {
        try {
            log.info("상호작용 읽음 처리 API 호출: userId={}, interactionId={}", userDetails.getUserId(), interactionId);
            friendService.markInteractionAsRead(userDetails.getUser(), interactionId);
            return ResponseEntity.ok(ApiResponse.success("상호작용을 읽음 처리했습니다.", null));
        } catch (Exception e) {
            log.error("상호작용 읽음 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 받은 상호작용 모두 읽음 처리
     */
    @PutMapping("/interactions/read-all")
    @Operation(summary = "상호작용 모두 읽음 처리", description = "현재 사용자에게 온 모든 상호작용을 읽음 처리합니다")
    public ResponseEntity<ApiResponse<Void>> markAllInteractionsAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            log.info("상호작용 모두 읽음 처리 API 호출: userId={}", userDetails.getUserId());
            friendService.markAllReceivedInteractionsAsRead(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("모두 읽음 처리했습니다.", null));
        } catch (Exception e) {
            log.error("상호작용 모두 읽음 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 홈 데이터 조회 (레벨/마스코트 요약 + 좋아요 상태)
     */
    @GetMapping("/{friendId}/home")
    @Operation(summary = "친구 홈 조회", description = "친구 홈 화면에 필요한 요약 데이터(레벨, 좋아요 누적/오늘 상태 등)를 반환합니다")
    public ResponseEntity<ApiResponse<FriendHomeResponse>> getFriendHome(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long friendId
    ) {
        try {
            log.info("친구 홈 조회 API 호출: viewerId={}, friendId={}", userDetails.getUserId(), friendId);
            FriendHomeResponse body = friendService.getFriendHome(userDetails.getUser(), friendId);
            return ResponseEntity.ok(ApiResponse.success("친구 홈 데이터를 조회했습니다.", body));
        } catch (Exception e) {
            log.error("친구 홈 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 친구 관계 확인
     */
    @GetMapping("/check/{targetUserId}")
    @Operation(summary = "친구 관계 확인", description = "현재 사용자와 대상 사용자 간의 친구 관계를 확인합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFriendship(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId
    ) {
        try {
            log.info("친구 관계 확인 API 호출: userId={}, targetUserId={}", userDetails.getUserId(), targetUserId);
            
            // 자기 자신인지 확인
            boolean isSelf = userDetails.getUserId().equals(targetUserId);
            
            // 친구 관계 확인
            boolean isFriend = false;
            boolean hasPendingRequest = false;
            boolean hasSentRequest = false;
            
            if (!isSelf) {
                User targetUser = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new EntityNotFoundException("대상 사용자를 찾을 수 없습니다."));
                
                isFriend = friendService.isFriend(userDetails.getUser(), targetUser);
                hasPendingRequest = friendService.hasPendingRequest(userDetails.getUser(), targetUser);
                hasSentRequest = friendService.hasSentRequest(userDetails.getUser(), targetUser);
            }
            
            Map<String, Object> result = Map.of(
                "isSelf", isSelf,
                "isFriend", isFriend,
                "hasPendingRequest", hasPendingRequest,
                "hasSentRequest", hasSentRequest
            );
            
            return ResponseEntity.ok(ApiResponse.success("친구 관계를 확인했습니다.", result));
        } catch (Exception e) {
            log.error("친구 관계 확인 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
