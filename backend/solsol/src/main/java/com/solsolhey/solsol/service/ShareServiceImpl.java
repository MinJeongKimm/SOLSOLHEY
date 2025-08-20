package com.solsolhey.solsol.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solsolhey.solsol.common.exception.BusinessException;
import com.solsolhey.solsol.common.exception.EntityNotFoundException;
import com.solsolhey.solsol.dto.share.*;
import com.solsolhey.solsol.entity.*;
import com.solsolhey.solsol.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

/**
 * 소셜 공유 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShareServiceImpl implements ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final ShareTemplateRepository shareTemplateRepository;
    private final ShareImageRepository shareImageRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public ShareLinkResponse createShareLink(User currentUser, ShareLinkCreateRequest request) {
        log.info("공유 링크 생성: userId={}, target={}, refId={}", 
                currentUser.getUserId(), request.getTarget(), request.getRefId());

        // 만료 시간 설정
        LocalDateTime expiresAt = null;
        if (request.getExpiresInHours() != null && request.getExpiresInHours() > 0) {
            expiresAt = LocalDateTime.now().plusHours(request.getExpiresInHours());
        }

        // 공유 링크 생성
        ShareLink shareLink = ShareLink.builder()
                .user(currentUser)
                .targetType(request.getTarget())
                .referenceId(request.getRefId())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .expiresAt(expiresAt)
                .build();

        ShareLink savedShareLink = shareLinkRepository.save(shareLink);

        log.info("공유 링크 생성 완료: shareLinkId={}, shareCode={}", 
                savedShareLink.getShareLinkId(), savedShareLink.getShareCode());

        return ShareLinkResponse.from(savedShareLink, baseUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public ShareLinkResponse getShareLink(String shareCode) {
        log.info("공유 링크 조회: shareCode={}", shareCode);

        ShareLink shareLink = shareLinkRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new EntityNotFoundException("공유 링크", shareCode));

        if (!shareLink.isUsable()) {
            throw new BusinessException("유효하지 않거나 만료된 공유 링크입니다.");
        }

        return ShareLinkResponse.from(shareLink, baseUrl);
    }

    @Override
    public void handleShareLinkClick(String shareCode) {
        log.info("공유 링크 클릭 처리: shareCode={}", shareCode);

        ShareLink shareLink = shareLinkRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new EntityNotFoundException("공유 링크", shareCode));

        if (!shareLink.isUsable()) {
            throw new BusinessException("유효하지 않거나 만료된 공유 링크입니다.");
        }

        shareLink.incrementClickCount();
        shareLinkRepository.save(shareLink);

        log.info("공유 링크 클릭 처리 완료: shareCode={}, clickCount={}", 
                shareCode, shareLink.getClickCount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareLinkResponse> getUserShareLinks(User currentUser) {
        log.info("사용자 공유 링크 목록 조회: userId={}", currentUser.getUserId());

        List<ShareLink> shareLinks = shareLinkRepository.findByUserOrderByCreatedAtDesc(currentUser);

        return shareLinks.stream()
                .map(shareLink -> ShareLinkResponse.from(shareLink, baseUrl))
                .collect(Collectors.toList());
    }

    @Override
    public ShareImageResponse createShareImage(User currentUser, ShareImageCreateRequest request) {
        log.info("공유 이미지 생성: userId={}, template={}", 
                currentUser.getUserId(), request.getTemplate());

        // 템플릿 조회
        ShareTemplate template = shareTemplateRepository.findByTemplateName(request.getTemplate())
                .orElseThrow(() -> new EntityNotFoundException("템플릿", request.getTemplate()));

        if (!template.getIsActive()) {
            throw new BusinessException("비활성화된 템플릿입니다.");
        }

        // 이미지 데이터를 JSON으로 변환
        String imageDataJson;
        try {
            imageDataJson = objectMapper.writeValueAsString(request.getData());
        } catch (Exception e) {
            log.error("이미지 데이터 JSON 변환 실패: userId={}, template={}", 
                    currentUser.getUserId(), request.getTemplate(), e);
            throw new BusinessException("이미지 데이터 처리에 실패했습니다.");
        }

        // 실제 이미지 생성 로직은 여기서 구현
        // 현재는 더미 URL 생성
        String imageUrl = generateImageUrl(currentUser, template);

        // 공유 이미지 엔티티 생성
        ShareImage shareImage = ShareImage.builder()
                .user(currentUser)
                .template(template)
                .imageUrl(imageUrl)
                .imageData(imageDataJson)
                .fileSize(calculateImageFileSize(template))
                .build();

        ShareImage savedShareImage = shareImageRepository.save(shareImage);

        log.info("공유 이미지 생성 완료: shareImageId={}, imageUrl={}", 
                savedShareImage.getShareImageId(), imageUrl);

        return ShareImageResponse.from(savedShareImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareImageResponse> getUserShareImages(User currentUser) {
        log.info("사용자 공유 이미지 목록 조회: userId={}", currentUser.getUserId());

        List<ShareImage> shareImages = shareImageRepository.findByUserAndStatusOrderByCreatedAtDesc(
                currentUser, ShareImage.ImageStatus.ACTIVE);

        return shareImages.stream()
                .map(ShareImageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void handleImageDownload(Long shareImageId) {
        log.info("이미지 다운로드 처리: shareImageId={}", shareImageId);

        ShareImage shareImage = shareImageRepository.findById(shareImageId)
                .orElseThrow(() -> new EntityNotFoundException("공유 이미지", shareImageId));

        if (!shareImage.isActive()) {
            throw new BusinessException("삭제된 이미지입니다.");
        }

        shareImage.incrementDownloadCount();
        shareImageRepository.save(shareImage);

        log.info("이미지 다운로드 처리 완료: shareImageId={}, downloadCount={}", 
                shareImageId, shareImage.getDownloadCount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareTemplateResponse> getAvailableTemplates() {
        log.info("사용 가능한 템플릿 목록 조회");

        List<ShareTemplate> templates = shareTemplateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return ShareTemplateResponse.fromList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareTemplateResponse> getTemplatesByType(String templateType) {
        log.info("타입별 템플릿 조회: templateType={}", templateType);

        ShareTemplate.TemplateType type;
        try {
            type = ShareTemplate.TemplateType.valueOf(templateType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("유효하지 않은 템플릿 타입입니다: " + templateType);
        }

        List<ShareTemplate> templates = shareTemplateRepository.findByTemplateTypeAndIsActiveTrueOrderByCreatedAtDesc(type);
        return ShareTemplateResponse.fromList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public ShareStatsResponse getShareStats(User currentUser) {
        log.info("공유 통계 조회: userId={}", currentUser.getUserId());

        // 총 공유 수
        Long totalShares = shareLinkRepository.countByUser(currentUser);
        
        // 총 클릭 수
        Long totalClicks = shareLinkRepository.getTotalClicksByUser(currentUser);
        
        // 총 이미지 수
        Long totalImages = shareImageRepository.countByUser(currentUser);
        
        // 총 다운로드 수
        Long totalDownloads = shareImageRepository.getTotalDownloadsByUser(currentUser);

        // 타입별 공유 통계
        List<Object[]> shareStatsByType = shareLinkRepository.getShareStatsByType(currentUser);
        
        Long mascotShares = 0L;
        Long challengeShares = 0L;
        Long profileShares = 0L;
        Long achievementShares = 0L;

        for (Object[] stat : shareStatsByType) {
            ShareLink.ShareTargetType type = (ShareLink.ShareTargetType) stat[0];
            Long count = (Long) stat[1];
            
            switch (type) {
                case MASCOT -> mascotShares = count;
                case CHALLENGE -> challengeShares = count;
                case PROFILE -> profileShares = count;
                case ACHIEVEMENT -> achievementShares = count;
            }
        }

        return ShareStatsResponse.of(
                totalShares, 
                totalClicks != null ? totalClicks : 0L,
                totalImages,
                totalDownloads != null ? totalDownloads : 0L,
                mascotShares, challengeShares, profileShares, achievementShares
        );
    }

    @Override
    public void deactivateShareLink(User currentUser, Long shareLinkId) {
        log.info("공유 링크 비활성화: userId={}, shareLinkId={}", currentUser.getUserId(), shareLinkId);

        ShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new EntityNotFoundException("공유 링크", shareLinkId));

        // 소유자 확인
        if (!shareLink.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new BusinessException("비활성화 권한이 없습니다.");
        }

        shareLink.deactivate();
        shareLinkRepository.save(shareLink);

        log.info("공유 링크 비활성화 완료: shareLinkId={}", shareLinkId);
    }

    @Override
    @Transactional
    public void cleanupExpiredLinks() {
        log.info("만료된 공유 링크 정리 시작");

        LocalDateTime now = LocalDateTime.now();
        List<ShareLink> expiredLinks = shareLinkRepository.findExpiredLinks(now);

        for (ShareLink shareLink : expiredLinks) {
            shareLink.deactivate();
        }

        if (!expiredLinks.isEmpty()) {
            shareLinkRepository.saveAll(expiredLinks);
        }

        log.info("만료된 공유 링크 정리 완료: count={}", expiredLinks.size());
    }

    /**
     * 이미지 URL 생성 (실제 이미지 생성 로직으로 대체 필요)
     */
    private String generateImageUrl(User user, ShareTemplate template) {
        // 실제 구현에서는 이미지 생성 서비스를 호출하여 실제 이미지 URL을 반환
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("%s/api/v1/images/share/%s_%s_%s.png", 
                baseUrl, user.getUserId(), template.getTemplateId(), timestamp);
    }

    /**
     * 이미지 파일 크기 계산 (실제 이미지 생성 후 크기 측정으로 대체 필요)
     */
    private Long calculateImageFileSize(ShareTemplate template) {
        // 더미 파일 크기 계산 (실제로는 생성된 이미지의 크기를 측정)
        long baseSize = (long) template.getWidth() * template.getHeight() * 3; // RGB 기준
        return baseSize / 10; // 압축 고려한 대략적인 크기
    }
}
