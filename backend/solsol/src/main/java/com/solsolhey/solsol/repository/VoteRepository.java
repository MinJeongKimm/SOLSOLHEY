package com.solsolhey.solsol.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.solsol.entity.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * 특정 사용자가 특정 참가자에게 투표했는지 확인
     */
    boolean existsByVoterIdAndEntryId(Long voterId, Long entryId);

    /**
     * 특정 참가자에 대한 모든 투표 조회
     */
    List<Vote> findByEntryId(Long entryId);

    /**
     * 특정 사용자의 투표 내역 조회
     */
    List<Vote> findByVoterId(Long voterId);

    /**
     * 특정 콘테스트 타입에 대한 투표 수 조회
     */
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.contestType = :contestType")
    long countByContestType(@Param("contestType") String contestType);

    /**
     * 특정 참가자에 대한 투표 수 조회
     */
    long countByEntryId(Long entryId);
}
