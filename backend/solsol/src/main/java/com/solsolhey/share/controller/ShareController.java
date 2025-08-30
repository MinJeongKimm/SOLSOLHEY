package com.solsolhey.share.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.share.dto.request.ShareImageCreateRequest;
import com.solsolhey.share.dto.request.ShareLinkCreateRequest;
import com.solsolhey.share.dto.response.*;
import com.solsolhey.share.service.ShareService;
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

/**
 * 공유 기능 API Controller
 */
@RestController
@RequestMapping("/api/v1/shares")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Share API", description = "공유 기능 관련 API")
public class ShareController {

    private final ShareService shareService;

    /**
     * 공유 링크 생성
     */
    @PostMapping("/links")
    @Operation(summary = "공유 링크 생성", description = "사용자가 공유 링크를 생성합니다")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShareLinkCreateRequest request) {
        try {
            log.info("공유 링크 생성 API 호출: userId={}", userDetails.getUserId());
            ShareLinkResponse response = shareService.createShareLink(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("공유 링크를 생성했습니다.", response));
        } catch (Exception e) {
            log.error("공유 링크 생성 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 링크 조회 (코드로)
     */
    @GetMapping("/links/{linkCode}")
    @Operation(summary = "공유 링크 조회", description = "링크 코드로 공유 링크 정보를 조회합니다")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> getShareLinkByCode(@PathVariable String linkCode) {
        try {
            log.info("공유 링크 조회 API 호출: linkCode={}", linkCode);
            ShareLinkResponse response = shareService.getShareLinkByCode(linkCode);
            
            // 공유 링크 조회 시 클릭 카운트 증가
            shareService.clickShareLink(linkCode);
            
            return ResponseEntity.ok(ApiResponse.success("공유 링크를 조회했습니다.", response));
        } catch (Exception e) {
            log.error("공유 링크 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 링크 클릭 처리
     */
    @PostMapping("/links/{linkCode}/click")
    @Operation(summary = "공유 링크 클릭 처리", description = "공유 링크 클릭을 처리하고 통계를 업데이트합니다")
    public ResponseEntity<ApiResponse<Void>> clickShareLink(@PathVariable String linkCode) {
        try {
            log.info("공유 링크 클릭 API 호출: linkCode={}", linkCode);
            shareService.clickShareLink(linkCode);
            return ResponseEntity.ok(ApiResponse.success("공유 링크 클릭을 처리했습니다.", null));
        } catch (Exception e) {
            log.error("공유 링크 클릭 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 사용자의 공유 링크 목록 조회
     */
    @GetMapping("/links")
    @Operation(summary = "사용자 공유 링크 목록", description = "현재 사용자가 생성한 공유 링크 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ShareLinkResponse>>> getUserShareLinks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("사용자 공유 링크 목록 API 호출: userId={}", userDetails.getUserId());
            Page<ShareLinkResponse> links = shareService.getUserShareLinks(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("공유 링크 목록을 조회했습니다.", links));
        } catch (Exception e) {
            log.error("공유 링크 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 링크 비활성화
     */
    @PutMapping("/links/{linkId}/deactivate")
    @Operation(summary = "공유 링크 비활성화", description = "사용자가 생성한 공유 링크를 비활성화합니다")
    public ResponseEntity<ApiResponse<Void>> deactivateShareLink(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long linkId) {
        try {
            log.info("공유 링크 비활성화 API 호출: userId={}, linkId={}", userDetails.getUserId(), linkId);
            shareService.deactivateShareLink(userDetails.getUser(), linkId);
            return ResponseEntity.ok(ApiResponse.success("공유 링크를 비활성화했습니다.", null));
        } catch (Exception e) {
            log.error("공유 링크 비활성화 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 이미지 생성
     */
    @PostMapping("/images")
    @Operation(summary = "공유 이미지 생성", description = "사용자가 공유 이미지를 생성합니다")
    public ResponseEntity<ApiResponse<ShareImageResponse>> createShareImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShareImageCreateRequest request) {
        try {
            log.info("공유 이미지 생성 API 호출: userId={}", userDetails.getUserId());
            ShareImageResponse response = shareService.createShareImage(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("공유 이미지를 생성했습니다.", response));
        } catch (Exception e) {
            log.error("공유 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 사용자의 공유 이미지 목록 조회
     */
    @GetMapping("/images")
    @Operation(summary = "사용자 공유 이미지 목록", description = "현재 사용자가 생성한 공유 이미지 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ShareImageResponse>>> getUserShareImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("사용자 공유 이미지 목록 API 호출: userId={}", userDetails.getUserId());
            Page<ShareImageResponse> images = shareService.getUserShareImages(userDetails.getUser(), pageable);
            return ResponseEntity.ok(ApiResponse.success("공유 이미지 목록을 조회했습니다.", images));
        } catch (Exception e) {
            log.error("공유 이미지 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공개 공유 이미지 목록 조회 (인기순)
     */
    @GetMapping("/images/public")
    @Operation(summary = "공개 공유 이미지 목록", description = "공개된 공유 이미지 목록을 인기순으로 조회합니다")
    public ResponseEntity<ApiResponse<Page<ShareImageResponse>>> getPublicShareImages(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("공개 공유 이미지 목록 API 호출");
            Page<ShareImageResponse> images = shareService.getPublicShareImages(pageable);
            return ResponseEntity.ok(ApiResponse.success("공개 공유 이미지 목록을 조회했습니다.", images));
        } catch (Exception e) {
            log.error("공개 공유 이미지 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 이미지 공개/비공개 토글
     */
    @PutMapping("/images/{imageId}/toggle-visibility")
    @Operation(summary = "공유 이미지 공개/비공개 토글", description = "공유 이미지의 공개/비공개 상태를 변경합니다")
    public ResponseEntity<ApiResponse<Void>> toggleImageVisibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId) {
        try {
            log.info("공유 이미지 공개/비공개 토글 API 호출: userId={}, imageId={}", userDetails.getUserId(), imageId);
            shareService.toggleImageVisibility(userDetails.getUser(), imageId);
            return ResponseEntity.ok(ApiResponse.success("공유 이미지 공개/비공개를 변경했습니다.", null));
        } catch (Exception e) {
            log.error("공유 이미지 공개/비공개 변경 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 이미지 공유 카운트 증가
     */
    @PostMapping("/images/{imageId}/share")
    @Operation(summary = "공유 이미지 공유 카운트 증가", description = "공유 이미지의 공유 카운트를 증가시킵니다")
    public ResponseEntity<ApiResponse<Void>> incrementImageShareCount(@PathVariable Long imageId) {
        try {
            log.info("공유 이미지 공유 카운트 증가 API 호출: imageId={}", imageId);
            shareService.incrementImageShareCount(imageId);
            return ResponseEntity.ok(ApiResponse.success("공유 카운트를 증가했습니다.", null));
        } catch (Exception e) {
            log.error("공유 카운트 증가 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 공유 템플릿 목록 조회
     */
    @GetMapping("/templates")
    @Operation(summary = "공유 템플릿 목록", description = "활성화된 공유 템플릿 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<ShareTemplateResponse>>> getActiveTemplates() {
        try {
            log.info("공유 템플릿 목록 API 호출");
            List<ShareTemplateResponse> templates = shareService.getActiveTemplates();
            return ResponseEntity.ok(ApiResponse.success("공유 템플릿 목록을 조회했습니다.", templates));
        } catch (Exception e) {
            log.error("공유 템플릿 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 템플릿 타입별 조회
     */
    @GetMapping("/templates/{templateType}")
    @Operation(summary = "템플릿 타입별 조회", description = "특정 타입의 공유 템플릿을 조회합니다")
    public ResponseEntity<ApiResponse<List<ShareTemplateResponse>>> getTemplatesByType(@PathVariable String templateType) {
        try {
            log.info("템플릿 타입별 조회 API 호출: templateType={}", templateType);
            List<ShareTemplateResponse> templates = shareService.getTemplatesByType(templateType);
            return ResponseEntity.ok(ApiResponse.success("템플릿을 조회했습니다.", templates));
        } catch (Exception e) {
            log.error("템플릿 타입별 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 사용자 공유 통계 조회
     */
    @GetMapping("/stats")
    @Operation(summary = "사용자 공유 통계", description = "현재 사용자의 공유 관련 통계를 조회합니다")
    public ResponseEntity<ApiResponse<ShareStatsResponse>> getUserShareStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("사용자 공유 통계 API 호출: userId={}", userDetails.getUserId());
            ShareStatsResponse stats = shareService.getUserShareStats(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("공유 통계를 조회했습니다.", stats));
        } catch (Exception e) {
            log.error("공유 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 전체 공유 통계 조회 (관리자용)
     */
    @GetMapping("/stats/total")
    @Operation(summary = "전체 공유 통계", description = "전체 공유 통계를 조회합니다 (관리자용)")
    public ResponseEntity<ApiResponse<ShareStatsResponse>> getTotalShareStats() {
        try {
            log.info("전체 공유 통계 API 호출");
            ShareStatsResponse stats = shareService.getTotalShareStats();
            return ResponseEntity.ok(ApiResponse.success("전체 공유 통계를 조회했습니다.", stats));
        } catch (Exception e) {
            log.error("전체 공유 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
