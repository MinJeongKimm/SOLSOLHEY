package com.solsolhey.ranking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.ranking.dto.request.CampusRankingRequest;
import com.solsolhey.ranking.dto.request.NationalRankingRequest;
import com.solsolhey.ranking.dto.request.VoteRequest;
import com.solsolhey.ranking.dto.response.CampusInfo;
import com.solsolhey.ranking.dto.response.FilterInfo;
import com.solsolhey.ranking.dto.response.RankingEntryResponse;
import com.solsolhey.ranking.dto.response.RankingResponse;
import com.solsolhey.ranking.dto.response.VoteResponse;
import com.solsolhey.ranking.entity.Vote;
import com.solsolhey.ranking.repository.VoteRepository;
import com.solsolhey.ranking.repository.RankingEntryRepository;
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
        
        // 해당 캠퍼스의 마스코트들을 조회하고 투표 수 기준으로 정렬
        List<Mascot> mascots = mascotRepository.findByUserCampus(targetCampus);
        
        List<RankingEntryResponse> entryResponses = buildCampusEntryResponses(mascots, targetCampus, request.getSort());

        long totalCount = mascots.size();

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
        
        List<Mascot> mascots;
        if (request.getSchoolId() != null) {
            // 특정 학교 필터링
            String schoolName = getCampusNameById(request.getSchoolId());
            mascots = mascotRepository.findByUserCampus(schoolName);
        } else {
            // 전체 전국 마스코트
            mascots = mascotRepository.findAll();
        }

        List<RankingEntryResponse> entryResponses = buildNationalEntryResponses(mascots, request.getSort());

        long totalCount = mascots.size();

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
    public VoteResponse voteForCampus(Long mascotId, VoteRequest request, Long voterId, String userCampus) {
        log.info("교내 투표 요청 - mascotId: {}, voterId: {}, campus: {}", mascotId, voterId, userCampus);

        // 기본 유효성 검사
        if (!canVote(voterId, mascotId, Vote.VoteType.CAMPUS)) {
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

        Mascot mascot = getMascotById(mascotId);
        
        // 캠퍼스 권한 확인
        User mascotOwner = getUserById(mascot.getUserId());
        if (!userCampus.equals(mascotOwner.getCampus())) {
            return VoteResponse.failure("해당 캠퍼스 투표 권한이 없습니다.");
        }

        // 투표 처리
        return processVote(mascotId, voterId, request, Vote.VoteType.CAMPUS);
    }

    @Override
    public VoteResponse voteForNational(Long mascotId, VoteRequest request, Long voterId) {
        log.info("전국 투표 요청 - mascotId: {}, voterId: {}", mascotId, voterId);

        // 기본 유효성 검사
        if (!canVote(voterId, mascotId, Vote.VoteType.NATIONAL)) {
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
        return processVote(mascotId, voterId, request, Vote.VoteType.NATIONAL);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canVote(Long voterId, Long mascotId, Vote.VoteType voteType) {
        // 중복 투표 확인
        if (hasAlreadyVoted(voterId, mascotId)) {
            return false;
        }

        // 자기 자신의 마스코트인지 확인
        Mascot mascot = getMascotById(mascotId);
        if (mascot.getUserId().equals(voterId)) {
            return false;
        }

        return true; // 마스코트가 존재하면 투표 가능
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

    @Override
    @Transactional(readOnly = true)
    public boolean hasAlreadyVoted(Long voterId, Long mascotId) {
        return voteRepository.existsByVoterIdAndMascotId(voterId, mascotId);
    }

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

    private VoteResponse processVote(Long mascotId, Long voterId, VoteRequest request, Vote.VoteType voteType) {
        try {
            // 투표 생성
            Vote vote = Vote.builder()
                    .mascotId(mascotId)
                    .voterId(voterId)
                    .voteType(voteType)
                    .weight(request.getWeight())
                    .idempotencyKey(request.getIdempotencyKey())
                    .campusId(request.getCampusId())
                    .build();

            voteRepository.save(vote);

            // 응답 생성
            if (voteType == Vote.VoteType.CAMPUS) {
                return VoteResponse.successForCampus(
                    mascotId, 
                    request.getCampusId(), 
                    getVoteCount(mascotId, voteType),
                    LocalDateTime.now()
                );
            } else {
                return VoteResponse.successForNational(
                    mascotId,
                    getVoteCount(mascotId, voteType),
                    LocalDateTime.now()
                );
            }

        } catch (Exception e) {
            log.error("투표 처리 실패 - mascotId: {}, voterId: {}", mascotId, voterId, e);
            return VoteResponse.failure("투표 처리 중 오류가 발생했습니다.");
        }
    }

    private long getVoteCount(Long mascotId, Vote.VoteType voteType) {
        return voteRepository.countByMascotIdAndVoteType(mascotId, voteType);
    }

    private List<RankingEntryResponse> buildCampusEntryResponses(List<Mascot> mascots, String campus, String sortType) {
        // 1. 정렬 타입에 따른 정렬 로직 적용
        List<RankingEntryResponse> sortedEntries = IntStream.range(0, mascots.size())
                .mapToObj(i -> {
                    Mascot mascot = mascots.get(i);
                    User owner = getUserById(mascot.getUserId());
                    long voteCount = getVoteCount(mascot.getId(), Vote.VoteType.CAMPUS);
                    return RankingEntryResponse.fromCampus(mascot, 0, owner.getNickname(), voteCount); // 임시 순위 0
                })
                .sorted((a, b) -> {
                    if ("newest".equals(sortType)) {
                        // 최신순: 마스코트 생성일 기준 내림차순
                        Mascot mascotA = mascots.stream()
                            .filter(m -> m.getId().equals(a.getMascotId()))
                            .findFirst()
                            .orElse(null);
                        Mascot mascotB = mascots.stream()
                            .filter(m -> m.getId().equals(b.getMascotId()))
                            .findFirst()
                            .orElse(null);
                        
                        if (mascotA != null && mascotB != null) {
                            return mascotB.getCreatedAt().compareTo(mascotA.getCreatedAt());
                        }
                        return 0;
                    } else {
                        // 기본값: 득표순 (votes_desc)
                        return Long.compare(b.getVotes(), a.getVotes());
                    }
                })
                .toList();
        
        // 2. 정렬된 순서대로 순위 재계산하여 새로운 객체 생성
        List<RankingEntryResponse> rankedEntries = new ArrayList<>();
        for (int i = 0; i < sortedEntries.size(); i++) {
            RankingEntryResponse entry = sortedEntries.get(i);
            // 원본 Mascot 객체를 찾아서 사용
            Mascot originalMascot = mascots.stream()
                .filter(m -> m.getId().equals(entry.getMascotId()))
                .findFirst()
                .orElse(null);
            
            RankingEntryResponse rankedEntry = RankingEntryResponse.fromCampus(
                originalMascot,
                i + 1, // 실제 순위
                entry.getOwnerNickname(),
                entry.getVotes()
            );
            rankedEntries.add(rankedEntry);
        }
        
        return rankedEntries;
    }

    private List<RankingEntryResponse> buildNationalEntryResponses(List<Mascot> mascots, String sortType) {
        // 1. 정렬 타입에 따른 정렬 로직 적용
        List<RankingEntryResponse> sortedEntries = IntStream.range(0, mascots.size())
                .mapToObj(i -> {
                    Mascot mascot = mascots.get(i);
                    User owner = getUserById(mascot.getUserId());
                    long voteCount = getVoteCount(mascot.getId(), Vote.VoteType.NATIONAL);
                    return RankingEntryResponse.fromNational(
                        mascot, 0, owner.getNickname(), owner.getCampus(), null, voteCount); // 임시 순위 0
                })
                .sorted((a, b) -> {
                    if ("newest".equals(sortType)) {
                        // 최신순: 마스코트 생성일 기준 내림차순
                        Mascot mascotA = mascots.stream()
                            .filter(m -> m.getId().equals(a.getMascotId()))
                            .findFirst()
                            .orElse(null);
                        Mascot mascotB = mascots.stream()
                            .filter(m -> m.getId().equals(b.getMascotId()))
                            .findFirst()
                            .orElse(null);
                        
                        if (mascotA != null && mascotB != null) {
                            return mascotB.getCreatedAt().compareTo(mascotA.getCreatedAt());
                        }
                        return 0;
                    } else {
                        // 기본값: 득표순 (votes_desc)
                        return Long.compare(b.getVotes(), a.getVotes());
                    }
                })
                .toList();
        
        // 2. 정렬된 순서대로 순위 재계산하여 새로운 객체 생성
        List<RankingEntryResponse> rankedEntries = new ArrayList<>();
        for (int i = 0; i < sortedEntries.size(); i++) {
            RankingEntryResponse entry = sortedEntries.get(i);
            // 원본 Mascot 객체를 찾아서 사용
            Mascot originalMascot = mascots.stream()
                .filter(m -> m.getId().equals(entry.getMascotId()))
                .findFirst()
                .orElse(null);
            
            RankingEntryResponse rankedEntry = RankingEntryResponse.fromNational(
                originalMascot,
                i + 1, // 실제 순위
                entry.getOwnerNickname(),
                originalMascot != null ? getUserById(originalMascot.getUserId()).getCampus() : null,
                null,
                entry.getVotes()
            );
            rankedEntries.add(rankedEntry);
        }
        
        return rankedEntries;
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
