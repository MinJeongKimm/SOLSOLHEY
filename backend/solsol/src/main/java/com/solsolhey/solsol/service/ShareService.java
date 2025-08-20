package com.solsolhey.solsol.service;

import com.solsolhey.solsol.dto.share.*;
import com.solsolhey.solsol.entity.User;

import java.util.List;

/**
 * 소셜 공유 서비스 인터페이스
 */
public interface ShareService {

    /**
     * 공유 링크 생성
     */
    ShareLinkResponse createShareLink(User currentUser, ShareLinkCreateRequest request);

    /**
     * 공유 링크 조회 (공유 코드로)
     */
    ShareLinkResponse getShareLink(String shareCode);

    /**
     * 공유 링크 클릭 처리
     */
    void handleShareLinkClick(String shareCode);

    /**
     * 사용자의 공유 링크 목록 조회
     */
    List<ShareLinkResponse> getUserShareLinks(User currentUser);

    /**
     * 공유 이미지 생성
     */
    ShareImageResponse createShareImage(User currentUser, ShareImageCreateRequest request);

    /**
     * 사용자의 공유 이미지 목록 조회
     */
    List<ShareImageResponse> getUserShareImages(User currentUser);

    /**
     * 이미지 다운로드 처리
     */
    void handleImageDownload(Long shareImageId);

    /**
     * 사용 가능한 템플릿 목록 조회
     */
    List<ShareTemplateResponse> getAvailableTemplates();

    /**
     * 특정 타입의 템플릿들 조회
     */
    List<ShareTemplateResponse> getTemplatesByType(String templateType);

    /**
     * 사용자의 공유 통계 조회
     */
    ShareStatsResponse getShareStats(User currentUser);

    /**
     * 공유 링크 비활성화
     */
    void deactivateShareLink(User currentUser, Long shareLinkId);

    /**
     * 만료된 공유 링크들 정리
     */
    void cleanupExpiredLinks();
}
