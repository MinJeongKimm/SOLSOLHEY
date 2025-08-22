package com.solsolhey.share.service;

import com.solsolhey.share.dto.request.ShareImageCreateRequest;
import com.solsolhey.share.dto.request.ShareLinkCreateRequest;
import com.solsolhey.share.dto.response.*;
import com.solsolhey.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 공유 서비스 인터페이스
 */
public interface ShareService {

    /**
     * 공유 링크 생성
     */
    ShareLinkResponse createShareLink(User user, ShareLinkCreateRequest request);

    /**
     * 공유 링크 조회 (코드로)
     */
    ShareLinkResponse getShareLinkByCode(String linkCode);

    /**
     * 공유 링크 클릭 처리
     */
    void clickShareLink(String linkCode);

    /**
     * 사용자의 공유 링크 목록 조회
     */
    Page<ShareLinkResponse> getUserShareLinks(User user, Pageable pageable);

    /**
     * 공유 링크 비활성화
     */
    void deactivateShareLink(User user, Long linkId);

    /**
     * 공유 이미지 생성
     */
    ShareImageResponse createShareImage(User user, ShareImageCreateRequest request);

    /**
     * 사용자의 공유 이미지 목록 조회
     */
    Page<ShareImageResponse> getUserShareImages(User user, Pageable pageable);

    /**
     * 공개 공유 이미지 목록 조회 (인기순)
     */
    Page<ShareImageResponse> getPublicShareImages(Pageable pageable);

    /**
     * 공유 이미지 공개/비공개 설정
     */
    void toggleImageVisibility(User user, Long imageId);

    /**
     * 공유 이미지 공유 카운트 증가
     */
    void incrementImageShareCount(Long imageId);

    /**
     * 공유 템플릿 목록 조회
     */
    List<ShareTemplateResponse> getActiveTemplates();

    /**
     * 템플릿 타입별 조회
     */
    List<ShareTemplateResponse> getTemplatesByType(String templateType);

    /**
     * 사용자 공유 통계 조회
     */
    ShareStatsResponse getUserShareStats(User user);

    /**
     * 전체 공유 통계 조회 (관리자용)
     */
    ShareStatsResponse getTotalShareStats();
}
