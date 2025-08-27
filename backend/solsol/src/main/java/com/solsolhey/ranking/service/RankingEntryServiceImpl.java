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

    private static final int MAX_ENTRIES_PER_USER = 3;

    @Override
    @Transactional
    public EntryResponse createEntry(Long userId, CreateEntryRequest request) {
        // 사용자당 최대 3개 제한 체크
        long currentEntryCount = getUserEntryCount(userId);
        if (currentEntryCount >= MAX_ENTRIES_PER_USER) {
            throw new BusinessException("사용자당 최대 3개까지만 참가할 수 있습니다.");
        }

        // 마스코트 스냅샷 ID가 0이 아닌 경우에만 중복 체크
        if (request.mascotSnapshotId() != null && request.mascotSnapshotId() > 0) {
            // 마스코트 스냅샷 중복 참가 체크
            if (isMascotSnapshotAlreadyParticipated(request.mascotSnapshotId())) {
                throw new BusinessException("이미 다른 사용자가 참가한 마스코트 스냅샷입니다.");
            }

            // 사용자가 해당 마스코트 스냅샷을 이미 참가했는지 체크
            if (hasUserParticipatedWithSnapshot(userId, request.mascotSnapshotId())) {
                throw new BusinessException("이미 참가한 마스코트 스냅샷입니다.");
            }
        }

        // RankingEntry 생성 및 저장
        RankingEntry entry = RankingEntry.builder()
            .userId(userId)
            .mascotSnapshotId(request.mascotSnapshotId())
            .title(request.title())
            .description(request.description())
            .imageUrl(request.imageUrl())
            .build();

        RankingEntry savedEntry = rankingEntryRepository.save(entry);
        return EntryResponse.from(savedEntry);
    }

    @Override
    public List<EntryResponse> getUserEntries(Long userId) {
        List<RankingEntry> entries = rankingEntryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return entries.stream()
            .map(EntryResponse::from)
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
            String uploadDir = "uploads/ranking";
            Path uploadPath = Paths.get(uploadDir);
            
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
            
            // URL 반환 (절대 경로)
            return "http://localhost:8080/uploads/ranking/" + filename;
            
        } catch (IOException e) {
            throw new BusinessException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
}
