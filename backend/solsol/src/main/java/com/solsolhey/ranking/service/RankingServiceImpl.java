package com.solsolhey.ranking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.CampusInfo;
import com.solsolhey.ranking.dto.response.FilterInfo;
import com.solsolhey.ranking.dto.response.RankingEntryResponse;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.entity.ContestEntry;
import com.solsolhey.ranking.entity.Vote;
import com.solsolhey.ranking.repository.ContestEntryRepository;
import com.solsolhey.ranking.repository.ContestRepository;
import com.solsolhey.ranking.repository.VoteRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 랭킹 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RankingServiceImpl implements RankingService {

    private final ContestEntryRepository contestEntryRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ContestRepository contestRepository;

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
        Page<ContestEntry> entries = getCampusEntriesBySortType(
            targetCampus, request.getSort(), pageable);

        List<RankingEntryResponse> entryResponses = buildCampusEntryResponses(entries.getContent());

        long totalCount = contestEntryRepository.countCampusEntries(
            ContestEntry.ContestType.CAMPUS, 
            ContestEntry.EntryStatus.ACTIVE, 
            targetCampus);

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
        Page<ContestEntry> entries;

        if (request.getSchoolId() != null) {
            // 특정 학교 필터링
            String schoolName = getCampusNameById(request.getSchoolId());
            entries = contestEntryRepository.findNationalRankingBySchool(
                ContestEntry.ContestType.NATIONAL,
                ContestEntry.EntryStatus.ACTIVE,
                schoolName,
                pageable
            );
        } else {
            // 전체 전국 랭킹
            entries = getNationalEntriesBySortType(request.getSort(), pageable);
        }

        List<RankingEntryResponse> entryResponses = buildNationalEntryResponses(entries.getContent());

        long totalCount = contestEntryRepository.countByContestTypeAndStatus(
            ContestEntry.ContestType.NATIONAL, 
            ContestEntry.EntryStatus.ACTIVE);

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
        if (!canVote(voterId, entryId, ContestEntry.ContestType.CAMPUS)) {
            return VoteResponse.failure("투표할 수 없는 상태입니다.");
        }

        // 일일 투표 한도 확인
        if (hasReachedDailyVoteLimit(voterId, ContestEntry.ContestType.CAMPUS)) {
            return VoteResponse.failure("오늘의 투표 한도를 초과했습니다. 내일 다시 시도해주세요.");
        }

        // 멱등키 중복 확인
        if (request.getIdempotencyKey() != null && isDuplicateIdempotencyKey(request.getIdempotencyKey())) {
            return VoteResponse.failure("중복 요청이 감지되었습니다.", request.getIdempotencyKey());
        }

        ContestEntry entry = getContestEntry(entryId);
        
        // 캠퍼스 권한 확인
        User entryOwner = getUserById(entry.getUserId());
        if (!userCampus.equals(entryOwner.getCampus())) {
            return VoteResponse.failure("해당 캠퍼스 투표 권한이 없습니다.");
        }

        // 투표 처리
        return processVote(entry, voterId, request, ContestEntry.ContestType.CAMPUS);
    }

    @Override
    public VoteResponse voteForNational(Long entryId, VoteRequest request, Long voterId) {
        log.info("전국 투표 요청 - entryId: {}, voterId: {}", entryId, voterId);

        // 기본 유효성 검사
        if (!canVote(voterId, entryId, ContestEntry.ContestType.NATIONAL)) {
            return VoteResponse.failure("투표할 수 없는 상태입니다.");
        }

        // 일일 투표 한도 확인
        if (hasReachedDailyVoteLimit(voterId, ContestEntry.ContestType.NATIONAL)) {
            return VoteResponse.failure("투표 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        // 멱등키 중복 확인
        if (request.getIdempotencyKey() != null && isDuplicateIdempotencyKey(request.getIdempotencyKey())) {
            return VoteResponse.failure("중복 요청이 감지되었습니다.", request.getIdempotencyKey());
        }

        ContestEntry entry = getContestEntry(entryId);

        // 투표 처리
        return processVote(entry, voterId, request, ContestEntry.ContestType.NATIONAL);
    }

    @Override
    public ContestEntry participateInContest(Long userId, Long mascotId, ContestEntry.ContestType contestType, String thumbnailUrl) {
        log.info("콘테스트 참가 요청 - userId: {}, mascotId: {}, contestType: {}", userId, mascotId, contestType);

        // 중복 참가 확인
        if (contestEntryRepository.existsByUserIdAndMascotIdAndContestTypeAndStatus(
                userId, mascotId, contestType, ContestEntry.EntryStatus.ACTIVE)) {
            throw new BusinessException("이미 참가한 콘테스트입니다.");
        }

        // 현재 활성 콘테스트 찾기
        Long contestId = null;
        if (contestType == ContestEntry.ContestType.CAMPUS) {
            contestId = contestRepository.findDefaultActiveCampusContest(LocalDateTime.now())
                    .map(contest -> contest.getContestId())
                    .orElse(null);
        } else if (contestType == ContestEntry.ContestType.NATIONAL) {
            contestId = contestRepository.findDefaultActiveNationalContest(LocalDateTime.now())
                    .map(contest -> contest.getContestId())
                    .orElse(null);
        }

        if (contestId == null) {
            throw new BusinessException("현재 진행중인 콘테스트가 없습니다.");
        }

        log.info("참가할 콘테스트 ID: {}", contestId);

        ContestEntry entry = ContestEntry.builder()
                .userId(userId)
                .mascotId(mascotId)
                .contestId(contestId)
                .contestType(contestType)
                .thumbnailUrl(thumbnailUrl)
                .build();

        return contestEntryRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canVote(Long voterId, Long entryId, ContestEntry.ContestType contestType) {
        // 중복 투표 확인
        if (hasAlreadyVoted(voterId, entryId)) {
            return false;
        }

        // 자기 자신의 엔트리인지 확인
        ContestEntry entry = getContestEntry(entryId);
        if (entry.getUserId().equals(voterId)) {
            return false;
        }

        // 엔트리가 활성 상태인지 확인
        return entry.getStatus() == ContestEntry.EntryStatus.ACTIVE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReachedDailyVoteLimit(Long voterId, ContestEntry.ContestType contestType) {
        LocalDateTime today = LocalDateTime.now();
        Long dailyVoteCount = voteRepository.countDailyVotesByVoter(voterId, contestType, today);
        
        int limit = contestType == ContestEntry.ContestType.CAMPUS ? 
            DAILY_CAMPUS_VOTE_LIMIT : DAILY_NATIONAL_VOTE_LIMIT;
            
        return dailyVoteCount >= limit;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAlreadyVoted(Long voterId, Long entryId) {
        return voteRepository.existsByVoterIdAndEntryId(voterId, entryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicateIdempotencyKey(String idempotencyKey) {
        return voteRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional(readOnly = true)
    public ContestEntry getContestEntry(Long entryId) {
        return contestEntryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 콘테스트 엔트리입니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContestEntry> getUserEntries(Long userId, ContestEntry.ContestType contestType) {
        return contestEntryRepository.findByUserIdAndContestType(userId, contestType);
    }

    // === Private Helper Methods ===

    private VoteResponse processVote(ContestEntry entry, Long voterId, VoteRequest request, 
                                   ContestEntry.ContestType contestType) {
        try {
            // 투표 생성
            Vote vote = Vote.builder()
                    .entryId(entry.getEntryId())
                    .voterId(voterId)
                    .contestType(contestType)
                    .weight(request.getWeight())
                    .idempotencyKey(request.getIdempotencyKey())
                    .campusId(request.getCampusId())
                    .contestId(request.getContestId())
                    .build();

            voteRepository.save(vote);

            // 엔트리 투표 수 업데이트
            entry.addVote(request.getWeight());
            contestEntryRepository.save(entry);

            // 응답 생성
            if (contestType == ContestEntry.ContestType.CAMPUS) {
                return VoteResponse.successForCampus(
                    entry.getEntryId(), 
                    request.getCampusId(), 
                    request.getContestId(),
                    entry.getVotes(),
                    LocalDateTime.now()
                );
            } else {
                return VoteResponse.successForNational(
                    entry.getEntryId(),
                    request.getContestId(),
                    entry.getVotes(),
                    LocalDateTime.now()
                );
            }

        } catch (Exception e) {
            log.error("투표 처리 실패 - entryId: {}, voterId: {}", entry.getEntryId(), voterId, e);
            return VoteResponse.failure("투표 처리 중 오류가 발생했습니다.");
        }
    }

    private Page<ContestEntry> getCampusEntriesBySortType(String campus, String sort, Pageable pageable) {
        ContestEntry.EntryStatus status = ContestEntry.EntryStatus.ACTIVE;
        ContestEntry.ContestType type = ContestEntry.ContestType.CAMPUS;

        return switch (sort) {
            case "trending" -> contestEntryRepository.findCampusRankingByTrend(type, status, campus, pageable);
            case "newest" -> contestEntryRepository.findCampusRankingByNewest(type, status, campus, pageable);
            default -> contestEntryRepository.findCampusRankingByVotes(type, status, campus, pageable);
        };
    }

    private Page<ContestEntry> getNationalEntriesBySortType(String sort, Pageable pageable) {
        ContestEntry.EntryStatus status = ContestEntry.EntryStatus.ACTIVE;
        ContestEntry.ContestType type = ContestEntry.ContestType.NATIONAL;

        return switch (sort) {
            case "trending" -> contestEntryRepository.findByContestTypeAndStatusOrderByTrendScoreDesc(type, status, pageable);
            case "newest" -> contestEntryRepository.findByContestTypeAndStatusOrderByCreatedAtDesc(type, status, pageable);
            default -> contestEntryRepository.findByContestTypeAndStatusOrderByVotesDesc(type, status, pageable);
        };
    }

    private List<RankingEntryResponse> buildCampusEntryResponses(List<ContestEntry> entries) {
        return IntStream.range(0, entries.size())
                .mapToObj(i -> {
                    ContestEntry entry = entries.get(i);
                    User owner = getUserById(entry.getUserId());
                    return RankingEntryResponse.fromCampus(entry, i + 1, owner.getNickname());
                })
                .toList();
    }

    private List<RankingEntryResponse> buildNationalEntryResponses(List<ContestEntry> entries) {
        return IntStream.range(0, entries.size())
                .mapToObj(i -> {
                    ContestEntry entry = entries.get(i);
                    User owner = getUserById(entry.getUserId());
                    return RankingEntryResponse.fromNational(
                        entry, i + 1, owner.getNickname(), owner.getCampus(), owner.getUserId());
                })
                .toList();
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
}
