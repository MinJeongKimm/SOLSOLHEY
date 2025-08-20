package com.solsolhey.solsol.controller;

import com.solsolhey.solsol.auth.dto.CustomUserDetails;
import com.solsolhey.solsol.common.response.ApiResponse;
import com.solsolhey.solsol.dto.share.*;
import com.solsolhey.solsol.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 소셜 공유 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
@Slf4j
public class ShareController {

    private final ShareService shareService;

    /**
     * 공유 링크 생성
     */
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @Valid @RequestBody ShareLinkCreateRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("공유 링크 생성 요청: userId={}, target={}", userDetails.getUserId(), request.getTarget());

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
            ShareLinkResponse response = shareService.createShareLink(userDetails.getUser(), request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("공유 링크가 생성되었습니다.", response));
                    
        } catch (Exception e) {
            log.error("공유 링크 생성 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 공유 이미지 생성
     */
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<ShareImageResponse>> createShareImage(
            @Valid @RequestBody ShareImageCreateRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("공유 이미지 생성 요청: userId={}, template={}", userDetails.getUserId(), request.getTemplate());

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
            ShareImageResponse response = shareService.createShareImage(userDetails.getUser(), request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("공유 이미지가 생성되었습니다.", response));
                    
        } catch (Exception e) {
            log.error("공유 이미지 생성 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 공유 링크 조회 (공개 API - 인증 불필요)
     */
    @GetMapping("/link/{shareCode}")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> getShareLink(@PathVariable String shareCode) {
        log.info("공유 링크 조회: shareCode={}", shareCode);

        try {
            ShareLinkResponse response = shareService.getShareLink(shareCode);
            
            return ResponseEntity.ok(ApiResponse.success("공유 링크를 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("공유 링크 조회 실패: shareCode={}", shareCode, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        }
    }

    /**
     * 공유 링크 클릭 처리 (공개 API - 인증 불필요)
     */
    @PostMapping("/link/{shareCode}/click")
    public ResponseEntity<ApiResponse<String>> handleShareLinkClick(@PathVariable String shareCode) {
        log.info("공유 링크 클릭: shareCode={}", shareCode);

        try {
            shareService.handleShareLinkClick(shareCode);
            
            return ResponseEntity.ok(ApiResponse.success("클릭이 처리되었습니다.", "처리 완료"));
                    
        } catch (Exception e) {
            log.error("공유 링크 클릭 처리 실패: shareCode={}", shareCode, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 사용자의 공유 링크 목록 조회
     */
    @GetMapping("/links")
    public ResponseEntity<ApiResponse<List<ShareLinkResponse>>> getUserShareLinks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("사용자 공유 링크 목록 조회: userId={}", userDetails.getUserId());

        try {
            List<ShareLinkResponse> response = shareService.getUserShareLinks(userDetails.getUser());
            
            return ResponseEntity.ok(ApiResponse.success("공유 링크 목록을 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("공유 링크 목록 조회 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "공유 링크 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 사용자의 공유 이미지 목록 조회
     */
    @GetMapping("/images")
    public ResponseEntity<ApiResponse<List<ShareImageResponse>>> getUserShareImages(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("사용자 공유 이미지 목록 조회: userId={}", userDetails.getUserId());

        try {
            List<ShareImageResponse> response = shareService.getUserShareImages(userDetails.getUser());
            
            return ResponseEntity.ok(ApiResponse.success("공유 이미지 목록을 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("공유 이미지 목록 조회 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "공유 이미지 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 이미지 다운로드 처리 (공개 API - 인증 불필요)
     */
    @PostMapping("/image/{shareImageId}/download")
    public ResponseEntity<ApiResponse<String>> handleImageDownload(@PathVariable Long shareImageId) {
        log.info("이미지 다운로드 처리: shareImageId={}", shareImageId);

        try {
            shareService.handleImageDownload(shareImageId);
            
            return ResponseEntity.ok(ApiResponse.success("다운로드가 처리되었습니다.", "처리 완료"));
                    
        } catch (Exception e) {
            log.error("이미지 다운로드 처리 실패: shareImageId={}", shareImageId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 사용 가능한 템플릿 목록 조회
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ShareTemplateResponse>>> getAvailableTemplates() {
        log.info("사용 가능한 템플릿 목록 조회");

        try {
            List<ShareTemplateResponse> response = shareService.getAvailableTemplates();
            
            return ResponseEntity.ok(ApiResponse.success("템플릿 목록을 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("템플릿 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "템플릿 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 타입별 템플릿 조회
     */
    @GetMapping("/templates/{templateType}")
    public ResponseEntity<ApiResponse<List<ShareTemplateResponse>>> getTemplatesByType(
            @PathVariable String templateType) {

        log.info("타입별 템플릿 조회: templateType={}", templateType);

        try {
            List<ShareTemplateResponse> response = shareService.getTemplatesByType(templateType);
            
            return ResponseEntity.ok(ApiResponse.success("템플릿 목록을 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("타입별 템플릿 조회 실패: templateType={}", templateType, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 공유 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ShareStatsResponse>> getShareStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("공유 통계 조회: userId={}", userDetails.getUserId());

        try {
            ShareStatsResponse response = shareService.getShareStats(userDetails.getUser());
            
            return ResponseEntity.ok(ApiResponse.success("공유 통계를 조회했습니다.", response));
                    
        } catch (Exception e) {
            log.error("공유 통계 조회 실패: userId={}", userDetails.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "공유 통계 조회에 실패했습니다."));
        }
    }

    /**
     * 공유 링크 비활성화
     */
    @DeleteMapping("/link/{shareLinkId}")
    public ResponseEntity<ApiResponse<String>> deactivateShareLink(
            @PathVariable Long shareLinkId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("공유 링크 비활성화: userId={}, shareLinkId={}", userDetails.getUserId(), shareLinkId);

        try {
            shareService.deactivateShareLink(userDetails.getUser(), shareLinkId);
            
            return ResponseEntity.ok(ApiResponse.success("공유 링크가 비활성화되었습니다.", "비활성화 완료"));
                    
        } catch (Exception e) {
            log.error("공유 링크 비활성화 실패: userId={}, shareLinkId={}", userDetails.getUserId(), shareLinkId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}