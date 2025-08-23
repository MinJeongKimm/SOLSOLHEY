package com.solsolhey.ranking.repository;

import com.solsolhey.ranking.entity.Contest;
import com.solsolhey.ranking.entity.ContestEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 콘테스트 레포지토리
 */
@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    /**
     * 활성 콘테스트 조회
     */
    List<Contest> findByStatusOrderByStartDateDesc(Contest.ContestStatus status);

    /**
     * 콘테스트 타입별 활성 콘테스트 조회
     */
    List<Contest> findByTypeAndStatusOrderByStartDateDesc(
            ContestEntry.ContestType type, Contest.ContestStatus status);

    /**
     * 캠퍼스별 활성 콘테스트 조회
     */
    List<Contest> findByCampusIdAndStatusOrderByStartDateDesc(
            Long campusId, Contest.ContestStatus status);

    /**
     * 지역별 활성 콘테스트 조회
     */
    List<Contest> findByRegionAndStatusOrderByStartDateDesc(
            String region, Contest.ContestStatus status);

    /**
     * 현재 진행중인 콘테스트 조회
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.status = :status " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now " +
           "ORDER BY c.startDate DESC")
    List<Contest> findActiveContests(
            @Param("status") Contest.ContestStatus status,
            @Param("now") LocalDateTime now);

    /**
     * 콘테스트 타입별 현재 진행중인 콘테스트 조회
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.type = :type " +
           "AND c.status = :status " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now " +
           "ORDER BY c.startDate DESC")
    List<Contest> findActiveContestsByType(
            @Param("type") ContestEntry.ContestType type,
            @Param("status") Contest.ContestStatus status,
            @Param("now") LocalDateTime now);

    /**
     * 캠퍼스별 현재 진행중인 콘테스트 조회
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.campusId = :campusId " +
           "AND c.status = :status " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now " +
           "ORDER BY c.startDate DESC")
    List<Contest> findActiveCampusContests(
            @Param("campusId") Long campusId,
            @Param("status") Contest.ContestStatus status,
            @Param("now") LocalDateTime now);

    /**
     * 곧 시작할 콘테스트 조회
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.status = :status " +
           "AND c.startDate > :now " +
           "ORDER BY c.startDate ASC")
    List<Contest> findUpcomingContests(
            @Param("status") Contest.ContestStatus status,
            @Param("now") LocalDateTime now);

    /**
     * 최근 종료된 콘테스트 조회
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.status = :status " +
           "AND c.endDate <= :now " +
           "ORDER BY c.endDate DESC")
    Page<Contest> findRecentEndedContests(
            @Param("status") Contest.ContestStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * 기간별 콘테스트 조회
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.startDate >= :startDate " +
           "AND c.endDate <= :endDate " +
           "ORDER BY c.startDate DESC")
    List<Contest> findContestsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 이름으로 콘테스트 검색
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY c.startDate DESC")
    Page<Contest> findByNameContainingIgnoreCase(
            @Param("name") String name,
            Pageable pageable);

    /**
     * 콘테스트별 참가자 수 조회
     */
    @Query("SELECT c.contestId, COUNT(ce) as participantCount " +
           "FROM Contest c " +
           "LEFT JOIN ContestEntry ce ON c.contestId = ce.contestId " +
           "WHERE c.contestId IN :contestIds " +
           "GROUP BY c.contestId")
    List<Object[]> findParticipantCounts(@Param("contestIds") List<Long> contestIds);

    /**
     * 인기 콘테스트 조회 (참가자 수 기준)
     */
    @Query("SELECT c, COUNT(ce) as participantCount " +
           "FROM Contest c " +
           "LEFT JOIN ContestEntry ce ON c.contestId = ce.contestId " +
           "WHERE c.status = :status " +
           "GROUP BY c " +
           "ORDER BY participantCount DESC")
    Page<Object[]> findPopularContests(
            @Param("status") Contest.ContestStatus status,
            Pageable pageable);

    /**
     * 디폴트 활성 콘테스트 조회 (교내)
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.type = 'CAMPUS' " +
           "AND c.status = 'ACTIVE' " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now " +
           "ORDER BY c.startDate DESC " +
           "LIMIT 1")
    Optional<Contest> findDefaultActiveCampusContest(@Param("now") LocalDateTime now);

    /**
     * 디폴트 활성 콘테스트 조회 (전국)
     */
    @Query("SELECT c FROM Contest c " +
           "WHERE c.type = 'NATIONAL' " +
           "AND c.status = 'ACTIVE' " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now " +
           "ORDER BY c.startDate DESC " +
           "LIMIT 1")
    Optional<Contest> findDefaultActiveNationalContest(@Param("now") LocalDateTime now);
}
