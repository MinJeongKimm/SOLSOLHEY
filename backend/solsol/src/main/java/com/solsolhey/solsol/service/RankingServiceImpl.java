package com.solsolhey.solsol.service;

import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.solsol.common.exception.BusinessException;
import com.solsolhey.solsol.dto.ranking.RankingResponseDto;
import com.solsolhey.solsol.dto.ranking.VoteResponseDto;
import com.solsolhey.solsol.entity.ContestEntry;
import com.solsolhey.solsol.entity.User;
import com.solsolhey.solsol.entity.Vote;
import com.solsolhey.solsol.repository.ContestEntryRepository;
import com.solsolhey.solsol.repository.UserRepository;
import com.solsolhey.solsol.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final MascotRepository mascotRepository;

    @Override
    public List<RankingResponseDto> getCampusRankings(String sort) {
        log.info("교내 랭킹 조회 요청 - 정렬: {}", sort);
        return getRankingsByType(ContestEntry.ContestType.CAMPUS, sort);
    }

    @Override
    public List<RankingResponseDto> getNationalRankings(String sort) {
        log.info("전국 랭킹 조회 요청 - 정렬: {}", sort);
        return getRankingsByType(ContestEntry.ContestType.NATIONAL, sort);
    }

    @Override
    public VoteResponseDto voteForNationalRanking(Long entryId, Long voterId) {
        log.info("전국 랭킹 투표 요청 - entryId: {}, voterId: {}", entryId, voterId);

        // 이미 투표했는지 확인
        if (voteRepository.existsByVoterIdAndEntryId(voterId, entryId)) {
            return VoteResponseDto.failure("이미 투표한 참가자입니다.");
        }

        // 참가자 정보 조회
        ContestEntry entry = contestEntryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 참가자입니다."));

        // 전국 콘테스트가 아닌 경우
        if (entry.getContestType() != ContestEntry.ContestType.NATIONAL) {
            return VoteResponseDto.failure("전국 콘테스트에만 투표할 수 있습니다.");
        }

        // 투표 생성
        Vote vote = Vote.builder()
                .voterId(voterId)
                .entryId(entryId)
                .contestType(ContestEntry.ContestType.NATIONAL)
                .build();
        voteRepository.save(vote);

        // 참가자 점수 업데이트
        entry.addVote();
        contestEntryRepository.save(entry);

        log.info("투표 완료 - entryId: {}, voterId: {}", entryId, voterId);
        return VoteResponseDto.success(entryId, voterId, ContestEntry.ContestType.NATIONAL.name());
    }

    @Override
    public ContestEntry participateInContest(Long userId, Long mascotId, ContestEntry.ContestType contestType) {
        log.info("콘테스트 참가 요청 - userId: {}, mascotId: {}, contestType: {}", userId, mascotId, contestType);

        // 이미 참가했는지 확인
        if (contestEntryRepository.existsByUserIdAndMascotIdAndContestType(userId, mascotId, contestType)) {
            throw new BusinessException("이미 참가한 콘테스트입니다.");
        }

        // 참가자 생성
        ContestEntry entry = ContestEntry.builder()
                .userId(userId)
                .mascotId(mascotId)
                .contestType(contestType)
                .build();

        ContestEntry savedEntry = contestEntryRepository.save(entry);
        log.info("콘테스트 참가 완료 - entryId: {}", savedEntry.getEntryId());
        return savedEntry;
    }

    @Override
    public List<RankingResponseDto> getRankingsByType(ContestEntry.ContestType contestType, String sort) {
        log.info("랭킹 조회 - contestType: {}, sort: {}", contestType, sort);

        List<ContestEntry> entries = contestEntryRepository.findByContestTypeOrderByScoreDesc(contestType);

        return IntStream.range(0, entries.size())
                .mapToObj(i -> {
                    ContestEntry entry = entries.get(i);
                    User user = userRepository.findById(entry.getUserId())
                            .orElseThrow(() -> new BusinessException("사용자 정보를 찾을 수 없습니다."));

                    // 마스코트 정보 조회
                    Mascot mascot = mascotRepository.findById(entry.getMascotId())
                            .orElseThrow(() -> new BusinessException("마스코트 정보를 찾을 수 없습니다."));

                    return RankingResponseDto.of(
                            entry.getEntryId(),
                            entry.getUserId(),
                            user.getNickname(),
                            user.getCampus(),
                            entry.getMascotId(),
                            mascot.getName(),
                            mascot.getType(),
                            mascot.getEquippedItem() != null ? mascot.getEquippedItem() : "없음",
                            entry.getScore(),
                            entry.getVoteCount(),
                            i + 1,
                            contestType.name()
                    );
                })
                .collect(Collectors.toList());
    }
}
