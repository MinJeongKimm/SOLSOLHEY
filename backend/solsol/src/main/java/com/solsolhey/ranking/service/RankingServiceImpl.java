package com.solsolhey.ranking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.domain.MascotSnapshot;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.mascot.repository.MascotSnapshotRepository;
import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.CampusInfo;
import com.solsolhey.ranking.dto.response.FilterInfo;
import com.solsolhey.ranking.dto.response.RankingEntryResponse;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.entity.RankingEntry;
import com.solsolhey.ranking.entity.Vote;
import com.solsolhey.ranking.repository.RankingEntryRepository;
import com.solsolhey.ranking.repository.VoteRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 랭킹 서비스 구현체 (마스코트 기반)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RankingServiceImpl implements RankingService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final MascotRepository mascotRepository;
    private final MascotSnapshotRepository mascotSnapshotRepository;
    private final RankingEntryRepository rankingEntryRepository;

    // 일일 투표 제한 (교내: 10회, 전국: 5회)
    private static final int DAILY_CAMPUS_VOTE_LIMIT = 10;
    private static final int DAILY_NATIONAL_VOTE_LIMIT = 5;

    @Override
    @Transactional(readOnly = true)
    public RankingResponse getCampusRankings(CampusRankingRequest request, String userCampus) {
        log.info("교내 랭킹 조회 요청 - campus: {}, sort: {}, period: {}", 
                 request.getCampusId() != null ? request.getCampusId() : userCampus, 
                 request.getSort(), request.getPeriod());

        String targetCampus = request.getCampusId() != null ? 
            getCampusNameById(request.getCampusId()) : userCampus;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        // 해당 캠퍼스의 교내 랭킹 RankingEntry를 직접 조회
        List<RankingEntry> rankingEntries = rankingEntryRepository.findByRankingTypeOrderByCreatedAtDesc("CAMPUS");
        
        // 캠퍼스별로 필터링
        List<RankingEntry> campusEntries = rankingEntries.stream()
            .filter(entry -> {
                Mascot mascot = mascotRepository.findById(entry.getMascotId()).orElse(null);
                if (mascot == null) return false;
                User owner = userRepository.findById(mascot.getUserId()).orElse(null);
                return owner != null && targetCampus.equals(owner.getCampus());
            })
            .toList();
        
        List<RankingEntryResponse> entryResponses = buildCampusEntryResponsesFromEntries(campusEntries, targetCampus, request.getSort());

        long totalCount = campusEntries.size();

        CampusInfo campusInfo = new CampusInfo(request.getCampusId(), targetCampus);

        return RankingResponse.forCampus(
            (int) totalCount,
            request.getPage(),
            request.getSize(),
            request.getPeriod(),
            request.getSort(),
            campusInfo,
            entryResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RankingResponse getNationalRankings(NationalRankingRequest request) {
        log.info("전국 랭킹 조회 요청 - sort: {}, period: {}, region: {}, schoolId: {}", 
                 request.getSort(), request.getPeriod(), request.getRegion(), request.getSchoolId());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        // 전국 랭킹 RankingEntry를 직접 조회
        List<RankingEntry> rankingEntries = rankingEntryRepository.findByRankingTypeOrderByCreatedAtDesc("NATIONAL");
        
        // 학교별로 필터링
        List<RankingEntry> nationalEntries = rankingEntries.stream()
            .filter(entry -> {
                Mascot mascot = mascotRepository.findById(entry.getMascotId()).orElse(null);
                if (mascot == null) return false;
                
                if (request.getSchoolId() != null) {
                    // 특정 학교 필터링
                    String schoolName = getCampusNameById(request.getSchoolId());
                    User owner = userRepository.findById(mascot.getUserId()).orElse(null);
                    return owner != null && schoolName.equals(owner.getCampus());
                }
                return true; // 전체 전국
            })
            .toList();

        List<RankingEntryResponse> entryResponses = buildNationalEntryResponsesFromEntries(nationalEntries, request.getSort());

        long totalCount = nationalEntries.size();

        FilterInfo filters = FilterInfo.of(request.getRegion(), request.getSchoolId());

        return RankingResponse.forNational(
            (int) totalCount,
            request.getPage(),
            request.getSize(),
            request.getPeriod(),
            request.getSort(),
            filters,
            entryResponses
        );
    }

    @Override
    public VoteResponse voteForCampus(Long entryId, VoteRequest request, Long voterId, String userCampus) {
        log.info("교내 투표 요청 - entryId: {}, voterId: {}, campus: {}", entryId, voterId, userCampus);

        // 기본 유효성 검사
        if (!canVote(voterId, entryId, Vote.VoteType.CAMPUS)) {
            return VoteResponse.failure("투표할 수 없는 상태입니다.");
        }

        // 일일 투표 한도 확인
        if (hasReachedDailyVoteLimit(voterId, Vote.VoteType.CAMPUS)) {
            return VoteResponse.failure("오늘의 투표 한도를 초과했습니다. 내일 다시 시도해주세요.");
        }

        // 멱등키 중복 확인
        if (request.getIdempotencyKey() != null && isDuplicateIdempotencyKey(request.getIdempotencyKey())) {
            return VoteResponse.failure("중복 요청이 감지되었습니다.", request.getIdempotencyKey());
        }

        // RankingEntry에서 마스코트 정보 가져오기
        RankingEntry rankingEntry = rankingEntryRepository.findById(entryId)
            .orElseThrow(() -> new BusinessException("랭킹 엔트리를 찾을 수 없습니다."));
        Mascot mascot = getMascotById(rankingEntry.getMascotId());
        
        // 캠퍼스 권한 확인
        User mascotOwner = getUserById(mascot.getUserId());
        if (!userCampus.equals(mascotOwner.getCampus())) {
            return VoteResponse.failure("해당 캠퍼스 투표 권한이 없습니다.");
        }

        // 투표 처리
        return processVote(entryId, voterId, request, Vote.VoteType.CAMPUS);
    }

    @Override
    public VoteResponse voteForNational(Long entryId, VoteRequest request, Long voterId) {
        log.info("전국 투표 요청 - entryId: {}, voterId: {}", entryId, voterId);

        // 기본 유효성 검사
        if (!canVote(voterId, entryId, Vote.VoteType.NATIONAL)) {
            return VoteResponse.failure("투표할 수 없는 상태입니다.");
        }

        // 일일 투표 한도 확인
        if (hasReachedDailyVoteLimit(voterId, Vote.VoteType.NATIONAL)) {
            return VoteResponse.failure("투표 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        // 멱등키 중복 확인
        if (request.getIdempotencyKey() != null && isDuplicateIdempotencyKey(request.getIdempotencyKey())) {
            return VoteResponse.failure("중복 요청이 감지되었습니다.", request.getIdempotencyKey());
        }

        // 투표 처리
        return processVote(entryId, voterId, request, Vote.VoteType.NATIONAL);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canVote(Long voterId, Long entryId, Vote.VoteType voteType) {
        log.info("투표 가능 여부 체크 시작 - voterId: {}, entryId: {}, voteType: {}", voterId, entryId, voteType);
        
        // 투표 타입별로 중복 투표 확인 (엔트리별로 처리)
        boolean alreadyVoted = voteRepository.existsByVoterIdAndEntryIdAndVoteType(voterId, entryId, voteType);
        log.info("이미 투표했는지 확인 - alreadyVoted: {}", alreadyVoted);
        if (alreadyVoted) {
            log.info("이미 투표한 엔트리입니다 - voterId: {}, entryId: {}, voteType: {}", voterId, entryId, voteType);
            return false;
        }

        // RankingEntry가 존재하는지 확인 (랭킹에 참가한 엔트리만 투표 가능)
        boolean hasRankingEntry = rankingEntryRepository.existsById(entryId);
        log.info("랭킹 엔트리 존재 여부 확인 - hasRankingEntry: {}", hasRankingEntry);
        if (!hasRankingEntry) {
            log.info("랭킹에 참가하지 않은 엔트리입니다 - entryId: {}", entryId);
            return false;
        }
        
        // RankingEntry에서 마스코트 정보 가져오기
        RankingEntry rankingEntry = rankingEntryRepository.findById(entryId)
            .orElseThrow(() -> new BusinessException("랭킹 엔트리를 찾을 수 없습니다."));
        
        // 자기 자신의 마스코트인지 확인
        Mascot mascot = getMascotById(rankingEntry.getMascotId());
        boolean isOwnMascot = mascot.getUserId().equals(voterId);
        log.info("자기 자신의 마스코트인지 확인 - isOwnMascot: {}", isOwnMascot);
        if (isOwnMascot) {
            log.info("자기 자신의 마스코트에는 투표할 수 없습니다 - voterId: {}, entryId: {}", voterId, entryId);
            return false;
        }
        
        // 해당 투표 타입의 RankingEntry가 맞는지 확인
        boolean hasCorrectRankingType = rankingEntry.getRankingType().equals(voteType.name());
        log.info("랭킹 타입 일치 여부 확인 - hasCorrectRankingType: {}", hasCorrectRankingType);
        
        if (!hasCorrectRankingType) {
            log.info("해당 랭킹 타입에 참가하지 않은 엔트리입니다 - entryId: {}, voteType: {}", entryId, voteType);
            return false;
        }

        log.info("투표 가능합니다 - voterId: {}, entryId: {}, voteType: {}", voterId, entryId, voteType);
        return true; // 엔트리가 존재하고 랭킹에 참가했으면 투표 가능
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReachedDailyVoteLimit(Long voterId, Vote.VoteType voteType) {
        LocalDateTime today = LocalDateTime.now();
        Long dailyVoteCount = voteRepository.countDailyVotesByVoter(voterId, voteType, today);
        
        int limit = voteType == Vote.VoteType.CAMPUS ? 
            DAILY_CAMPUS_VOTE_LIMIT : DAILY_NATIONAL_VOTE_LIMIT;
            
        return dailyVoteCount >= limit;
    }

    // 투표 타입별로 중복 투표 체크하므로 이 메서드는 더 이상 사용하지 않음
    // @Override
    // @Transactional(readOnly = true)
    // public boolean hasAlreadyVoted(Long voterId, Long mascotId) {
    //     return voteRepository.existsByVoterIdAndMascotId(voterId, mascotId);
    // }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicateIdempotencyKey(String idempotencyKey) {
        return voteRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Mascot getMascotById(Long mascotId) {
        return mascotRepository.findById(mascotId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 마스코트입니다."));
    }

    // === Private Helper Methods ===

    private VoteResponse processVote(Long entryId, Long voterId, VoteRequest request, Vote.VoteType voteType) {
        try {
            log.info("투표 처리 시작 - entryId: {}, voterId: {}, voteType: {}", entryId, voterId, voteType);
            
            // RankingEntry에서 마스코트 ID 가져오기
            RankingEntry rankingEntry = rankingEntryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException("랭킹 엔트리를 찾을 수 없습니다."));
            Long mascotId = rankingEntry.getMascotId();
            
            // 투표 생성
            Vote vote = Vote.builder()
                    .entryId(entryId)
                    .mascotId(mascotId)
                    .voterId(voterId)
                    .voteType(voteType)
                    .weight(request.getWeight())
                    .idempotencyKey(request.getIdempotencyKey())
                    .campusId(request.getCampusId())
                    .build();

            log.info("투표 엔티티 생성 완료 - vote: {}", vote);
            voteRepository.save(vote);
            log.info("투표 저장 완료 - voteId: {}", vote.getVoteId());

            // 투표 후 총 투표 수 조회
            long totalVotes = getVoteCount(mascotId, voteType);
            log.info("투표 후 총 투표 수 - mascotId: {}, voteType: {}, totalVotes: {}", mascotId, voteType, totalVotes);

            // 응답 생성
            if (voteType == Vote.VoteType.CAMPUS) {
                return VoteResponse.successForCampus(
                    mascotId, 
                    request.getCampusId(), 
                    totalVotes,
                    LocalDateTime.now()
                );
            } else {
                return VoteResponse.successForNational(
                    mascotId,
                    totalVotes,
                    LocalDateTime.now()
                );
            }

        } catch (Exception e) {
            log.error("투표 처리 실패 - mascotId: {}, voterId: {}", mascotId, voterId, e);
            return VoteResponse.failure("투표 처리 중 오류가 발생했습니다.");
        }
    }

    private long getVoteCount(Long entryId, Vote.VoteType voteType) {
        long count = voteRepository.countByEntryIdAndVoteType(entryId, voteType);
        log.info("투표 수 조회 - entryId: {}, voteType: {}, count: {}", entryId, voteType, count);
        return count;
    }

    private List<RankingEntryResponse> buildCampusEntryResponsesFromEntries(List<RankingEntry> entries, String campus, String sortType) {
        // RankingEntry를 정렬 기준에 따라 정렬
        List<RankingEntry> sortedEntries = new ArrayList<>(entries);
        
        if ("newest".equals(sortType)) {
            // 최신순: 등록일 기준 내림차순
            sortedEntries.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } else {
            // 기본값: 득표순 (votes_desc)
            sortedEntries.sort((a, b) -> {
                long votesA = getVoteCount(a.getMascotId(), Vote.VoteType.CAMPUS);
                long votesB = getVoteCount(b.getMascotId(), Vote.VoteType.CAMPUS);
                return Long.compare(votesB, votesA);
            });
        }
        
        // 정렬된 순서대로 RankingEntryResponse 생성
        List<RankingEntryResponse> rankedEntries = new ArrayList<>();
        for (int i = 0; i < sortedEntries.size(); i++) {
            RankingEntry entry = sortedEntries.get(i);
            Mascot mascot = getMascotById(entry.getMascotId());
            User owner = getUserById(mascot.getUserId());
            long voteCount = getVoteCount(entry.getEntryId(), Vote.VoteType.CAMPUS);
            
            // 새로 등록한 엔트리(이미지 업로드)인지 확인
            if (entry.getImageUrl() != null && entry.getImageUrl().startsWith("http://localhost:8080/uploads/ranking/")) {
                // 이미지 업로드로 등록된 엔트리: 스냅샷 없이 처리
                RankingEntryResponse rankedEntry = RankingEntryResponse.fromWithEntry(
                    mascot,
                    null, // 스냅샷 없음
                    Integer.valueOf(i + 1), // 실제 순위
                    owner.getNickname(),
                    null, // schoolName
                    null, // schoolId
                    voteCount,
                    entry.getImageUrl(), // 등록 시 업로드한 이미지 사용
                    entry.getTitle() // 등록 시 설정한 제목 사용
                );
                rankedEntries.add(rankedEntry);
            } else {
                // 기존 스냅샷 기반 엔트리: 스냅샷 조회
                MascotSnapshot snapshot = mascotSnapshotRepository.findByMascotIdOrderByCreatedAtDesc(mascot.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
                
                if (snapshot == null) {
                    log.warn("마스코트 ID {}에 대한 스냅샷을 찾을 수 없습니다.", mascot.getId());
                    continue; // 스냅샷이 없는 마스코트는 건너뛰기
                }
                
                RankingEntryResponse rankedEntry = RankingEntryResponse.fromWithEntry(
                    mascot,
                    snapshot,
                    Integer.valueOf(i + 1), // 실제 순위
                    owner.getNickname(),
                    null, // schoolName
                    null, // schoolId
                    voteCount,
                    entry.getImageUrl(), // 등록 시 업로드한 이미지 사용
                    entry.getTitle() // 등록 시 설정한 제목 사용
                );
                rankedEntries.add(rankedEntry);
            }
        }
        
        return rankedEntries;
    }

    private List<RankingEntryResponse> buildCampusEntryResponses(List<Mascot> mascots, String campus, String sortType) {
        // 기존 메서드 내용 (사용하지 않음)
        return new ArrayList<>();
    }

    private List<RankingEntryResponse> buildNationalEntryResponsesFromEntries(List<RankingEntry> entries, String sortType) {
        // RankingEntry를 정렬 기준에 따라 정렬
        List<RankingEntry> sortedEntries = new ArrayList<>(entries);
        
        if ("newest".equals(sortType)) {
            // 최신순: 등록일 기준 내림차순
            sortedEntries.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } else {
            // 기본값: 득표순 (votes_desc)
            sortedEntries.sort((a, b) -> {
                long votesA = getVoteCount(a.getMascotId(), Vote.VoteType.NATIONAL);
                long votesB = getVoteCount(b.getMascotId(), Vote.VoteType.NATIONAL);
                return Long.compare(votesB, votesA);
            });
        }
        
        // 정렬된 순서대로 RankingEntryResponse 생성
        List<RankingEntryResponse> rankedEntries = new ArrayList<>();
        for (int i = 0; i < sortedEntries.size(); i++) {
            RankingEntry entry = sortedEntries.get(i);
            Mascot mascot = getMascotById(entry.getMascotId());
            User owner = getUserById(mascot.getUserId());
            long voteCount = getVoteCount(mascot.getId(), Vote.VoteType.NATIONAL);
            
            // 새로 등록한 엔트리(이미지 업로드)인지 확인
            if (entry.getImageUrl() != null && entry.getImageUrl().startsWith("http://localhost:8080/uploads/ranking/")) {
                // 이미지 업로드로 등록된 엔트리: 스냅샷 없이 처리
                RankingEntryResponse rankedEntry = RankingEntryResponse.fromWithEntry(
                    mascot,
                    null, // 스냅샷 없음
                    Integer.valueOf(i + 1), // 실제 순위
                    owner.getNickname(),
                    owner.getCampus(),
                    null, // schoolId
                    voteCount,
                    entry.getImageUrl(), // 등록 시 업로드한 이미지 사용
                    entry.getTitle() // 등록 시 설정한 제목 사용
                );
                rankedEntries.add(rankedEntry);
            } else {
                // 기존 스냅샷 기반 엔트리: 스냅샷 조회
                MascotSnapshot snapshot = mascotSnapshotRepository.findByMascotIdOrderByCreatedAtDesc(mascot.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
                
                if (snapshot == null) {
                    log.warn("마스코트 ID {}에 대한 스냅샷을 찾을 수 없습니다.", mascot.getId());
                    continue; // 스냅샷이 없는 마스코트는 건너뛰기
                }
                
                RankingEntryResponse rankedEntry = RankingEntryResponse.fromWithEntry(
                    mascot,
                    null, // 스냅샷 없음
                    Integer.valueOf(i + 1), // 실제 순위
                    owner.getNickname(),
                    owner.getCampus(),
                    null, // schoolId
                    voteCount,
                    entry.getImageUrl(), // 등록 시 업로드한 이미지 사용
                    entry.getTitle() // 등록 시 설정한 제목 사용
                );
                rankedEntries.add(rankedEntry);
            }
        }
        
        return rankedEntries;
    }

    private List<RankingEntryResponse> buildNationalEntryResponses(List<Mascot> mascots, String sortType) {
        // 기존 메서드 내용 (사용하지 않음)
        return new ArrayList<>();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));
    }

    private String getCampusNameById(Long campusId) {
        // 실제로는 Campus 엔티티가 있어야 하지만, 현재는 User.campus 필드를 사용
        // 임시로 캠퍼스 ID를 이름으로 변환하는 로직
        return "캠퍼스_" + campusId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserVotedMascotIdsForCampus(Long voterId) {
        log.info("사용자 교내 랭킹 투표 히스토리 조회 - voterId: {}", voterId);
        
        try {
            // 사용자가 교내 랭킹에 투표한 마스코트 ID를 가져옴 (CAMPUS 타입만)
            List<Vote> userVotes = voteRepository.findByVoterIdAndVoteTypeOrderByCreatedAtDesc(voterId, Vote.VoteType.CAMPUS);
            
            // 중복 제거하여 마스코트 ID 목록 반환
            List<Long> votedMascotIds = userVotes.stream()
                .map(Vote::getMascotId)
                .distinct()
                .toList();
            
            log.info("사용자 교내 랭킹 투표 히스토리 조회 완료 - voterId: {}, 투표한 마스코트 수: {}", 
                     voterId, votedMascotIds.size());
            
            return votedMascotIds;
            
        } catch (Exception e) {
            log.error("사용자 교내 랭킹 투표 히스토리 조회 실패 - voterId: {}", voterId, e);
            throw new BusinessException("교내 랭킹 투표 히스토리를 조회하는데 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserVotedMascotIdsForNational(Long voterId) {
        log.info("사용자 전국 랭킹 투표 히스토리 조회 - voterId: {}", voterId);
        
        try {
            // 사용자가 전국 랭킹에 투표한 마스코트 ID를 가져옴 (NATIONAL 타입만)
            List<Vote> userVotes = voteRepository.findByVoterIdAndVoteTypeOrderByCreatedAtDesc(voterId, Vote.VoteType.NATIONAL);
            
            // 중복 제거하여 마스코트 ID 목록 반환
            List<Long> votedMascotIds = userVotes.stream()
                .map(Vote::getMascotId)
                .distinct()
                .toList();
            
            log.info("사용자 전국 랭킹 투표 히스토리 조회 완료 - voterId: {}, 투표한 마스코트 수: {}", 
                     voterId, votedMascotIds.size());
            
            return votedMascotIds;
            
        } catch (Exception e) {
            log.error("사용자 전국 랭킹 투표 히스토리 조회 실패 - voterId: {}", voterId, e);
            throw new BusinessException("전국 랭킹 투표 히스토리를 조회하는데 실패했습니다.");
        }
    }
}
