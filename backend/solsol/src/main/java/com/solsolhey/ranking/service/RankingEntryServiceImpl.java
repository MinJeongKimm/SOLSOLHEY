package com.solsolhey.ranking.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.mascot.repository.MascotSnapshotRepository;
import com.solsolhey.ranking.dto.request.CreateEntryRequest;
import com.solsolhey.ranking.dto.response.EntryResponse;
import com.solsolhey.ranking.dto.response.LeaderboardResponse;
import com.solsolhey.ranking.entity.RankingEntry;
import com.solsolhey.ranking.repository.RankingEntryRepository;

/**
 * 랭킹 참가 관련 서비스 구현체
 */
@Service
@Transactional(readOnly = true)
public class RankingEntryServiceImpl implements RankingEntryService {

    @Autowired
    private RankingEntryRepository rankingEntryRepository;

    @Autowired
    private com.solsolhey.solsol.config.MediaStorageProperties mediaProps;

    @Autowired
    private MascotSnapshotRepository mascotSnapshotRepository;

    private static final int MAX_ENTRIES_PER_USER = 3;

    @Override
    @Transactional
    public EntryResponse createEntry(Long userId, CreateEntryRequest request) {
        // 랭킹 타입별로 최대 3개 제한 체크
        long currentEntryCount = getUserEntryCountByType(userId, request.rankingType());
        if (currentEntryCount >= MAX_ENTRIES_PER_USER) {
            throw new BusinessException("해당 랭킹 타입에 최대 3개까지만 참가할 수 있습니다.");
        }

        // 마스코트 스냅샷 ID가 있는 경우에만 중복 체크
        if (request.mascotSnapshotId() != null && request.mascotSnapshotId() > 0) {
            // 마스코트 스냅샷 중복 참가 체크
            if (isMascotSnapshotAlreadyParticipated(request.mascotSnapshotId())) {
                throw new BusinessException("동일한 외형으로 등록할 순 없어요.");
            }

            // 사용자가 해당 마스코트 스냅샷을 이미 참가했는지 체크
            if (hasUserParticipatedWithSnapshot(userId, request.mascotSnapshotId())) {
                throw new BusinessException("동일한 외형으로 등록할 순 없어요.");
            }
        }

        // 스냅샷 기반인 경우, 스냅샷 이미지 URL을 일관되게 사용하고 중복을 방지
        String finalImageUrl = request.imageUrl();
        if (request.mascotSnapshotId() != null && request.mascotSnapshotId() > 0) {
            var snapshot = mascotSnapshotRepository.findById(request.mascotSnapshotId())
                    .orElse(null);
            if (snapshot != null && snapshot.getImageUrl() != null && !snapshot.getImageUrl().isBlank()) {
                finalImageUrl = snapshot.getImageUrl();
            }
        }

        // 동일 이미지로 이미 참가했는지 확인 (사용자/랭킹타입 단위)
        if (finalImageUrl != null && !finalImageUrl.isBlank()) {
            if (rankingEntryRepository.existsByUserIdAndRankingTypeAndImageUrl(userId, request.rankingType(), finalImageUrl)) {
                throw new BusinessException("동일한 외형으로 등록할 순 없어요.");
            }
        }
        
        // RankingEntry 생성 및 저장
        RankingEntry entry = RankingEntry.builder()
            .userId(userId)
            .mascotId(request.mascotId())
            .mascotSnapshotId(request.mascotSnapshotId())
            .title(request.title())
            .description(request.description())
            .imageUrl(finalImageUrl)
            .rankingType(request.rankingType())
            .build();

        RankingEntry savedEntry = rankingEntryRepository.save(entry);
        EntryResponse resp = EntryResponse.from(savedEntry);
        String norm = normalizeUrl(resp.imageUrl());
        return new EntryResponse(
            resp.entryId(),
            resp.userId(),
            resp.mascotId(),
            resp.mascotSnapshotId(),
            resp.title(),
            resp.description(),
            norm,
            resp.rankingType(),
            resp.createdAt()
        );
    }

    @Override
    public List<EntryResponse> getUserEntries(Long userId) {
        List<RankingEntry> entries = rankingEntryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return entries.stream()
            .map(EntryResponse::from)
            .map(er -> new EntryResponse(
                er.entryId(), er.userId(), er.mascotId(), er.mascotSnapshotId(), er.title(), er.description(),
                normalizeUrl(er.imageUrl()), er.rankingType(), er.createdAt()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<EntryResponse> getUserEntriesByType(Long userId, String rankingType) {
        List<RankingEntry> entries = rankingEntryRepository.findByUserIdAndRankingTypeOrderByCreatedAtDesc(userId, rankingType);
        return entries.stream()
            .map(EntryResponse::from)
            .map(er -> new EntryResponse(
                er.entryId(), er.userId(), er.mascotId(), er.mascotSnapshotId(), er.title(), er.description(),
                normalizeUrl(er.imageUrl()), er.rankingType(), er.createdAt()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        RankingEntry entry = rankingEntryRepository.findById(entryId)
            .orElseThrow(() -> new BusinessException("참가 정보를 찾을 수 없습니다."));

        // 본인만 삭제 가능
        if (!entry.getUserId().equals(userId)) {
            throw new BusinessException("본인의 참가 정보만 삭제할 수 있습니다.");
        }

        rankingEntryRepository.delete(entry);
    }

    @Override
    public LeaderboardResponse getLeaderboard(Pageable pageable) {
        Page<RankingEntry> entryPage = rankingEntryRepository.findAllOrderByCreatedAtDesc(pageable);
        
        // 순위 계산 및 설정
        List<LeaderboardResponse.LeaderboardEntry> entries = entryPage.getContent().stream()
            .map(entry -> {
                LeaderboardResponse.LeaderboardEntry leaderboardEntry = LeaderboardResponse.LeaderboardEntry.from(entry);
                // 순위 계산 (페이지 기반)
                int rank = (int) (entryPage.getNumber() * entryPage.getSize()) + 
                           entryPage.getContent().indexOf(entry) + 1;
                // 투표수는 0으로 설정 (실제로는 Vote 테이블에서 계산해야 함)
                return leaderboardEntry.withRankAndVoteCount(rank, 0);
            })
            .collect(Collectors.toList());

        // 새로운 LeaderboardResponse 생성
        return new LeaderboardResponse(
            entries,
            entryPage.getNumber(),
            entryPage.getTotalPages(),
            entryPage.getTotalElements(),
            entryPage.hasNext(),
            entryPage.hasPrevious()
        );
    }

    @Override
    public long getUserEntryCount(Long userId) {
        return rankingEntryRepository.countByUserId(userId);
    }

    @Override
    public long getUserEntryCountByType(Long userId, String rankingType) {
        return rankingEntryRepository.countByUserIdAndRankingType(userId, rankingType);
    }

    @Override
    public boolean isMascotSnapshotAlreadyParticipated(Long mascotSnapshotId) {
        return rankingEntryRepository.existsByMascotSnapshotId(mascotSnapshotId);
    }

    @Override
    public boolean hasUserParticipatedWithSnapshot(Long userId, Long mascotSnapshotId) {
        return rankingEntryRepository.existsByUserIdAndMascotSnapshotId(userId, mascotSnapshotId);
    }

    @Override
    public String uploadMascotImage(MultipartFile mascotImage) {
        try {
            // 업로드 디렉토리 설정
            String rootDir = mediaProps.getUploadDir(); // e.g., ./uploads
            Path uploadPath = Paths.get(rootDir).toAbsolutePath().normalize().resolve("ranking");
            
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 고유한 파일명 생성
            String originalFilename = mascotImage.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
            String filename = "mascot_" + UUID.randomUUID().toString() + fileExtension;
            
            // 파일 저장
            Path filePath = uploadPath.resolve(filename);
            Files.copy(mascotImage.getInputStream(), filePath);
            
            // URL 반환
            String rel = "/uploads/ranking/" + filename;
            String base = mediaProps.getPublicBaseUrl();
            if (base != null && !base.isBlank()) {
                // trim trailing slash
                if (base.endsWith("/")) base = base.substring(0, base.length()-1);
                return base + rel;
            }
            return rel;

        } catch (IOException e) {
            throw new BusinessException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) return url;
        String base = mediaProps.getPublicBaseUrl();
        if (url.startsWith("http://localhost:8080")) {
            String path = url.substring("http://localhost:8080".length());
            if (base != null && !base.isBlank()) {
                if (base.endsWith("/")) base = base.substring(0, base.length()-1);
                return base + path;
            }
            return path;
        }
        if (url.startsWith("/uploads/")) {
            if (base != null && !base.isBlank()) {
                if (base.endsWith("/")) base = base.substring(0, base.length()-1);
                return base + url;
            }
        }
        return url;
    }

    @Autowired
    private MascotRepository mascotRepository;

    @Override
    public Long getCurrentUserMascotId(Long userId) {
        // 사용자의 마스코트 ID 조회
        Mascot mascot = mascotRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException("사용자의 마스코트를 찾을 수 없습니다."));
        return mascot.getId();
    }
}
