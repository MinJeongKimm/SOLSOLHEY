package com.solsolhey.mascot.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.mascot.domain.UserBackground;

/**
 * 사용자 보유 배경 레포지토리
 */
@Repository
public interface UserBackgroundRepository extends JpaRepository<UserBackground, Long> {

    /**
     * 사용자별 보유 배경 목록 조회
     */
    List<UserBackground> findByUserId(Long userId);

    /**
     * 사용자별 보유 배경 목록 조회 (획득일 내림차순)
     */
    List<UserBackground> findByUserIdOrderByAcquiredAtDesc(Long userId);

    /**
     * 사용자가 특정 배경을 보유하고 있는지 확인
     */
    boolean existsByUserIdAndBackgroundId(Long userId, String backgroundId);

    /**
     * 사용자의 특정 배경 정보 조회
     */
    Optional<UserBackground> findByUserIdAndBackgroundId(Long userId, String backgroundId);

    /**
     * 특정 배경을 보유한 사용자 목록 조회
     */
    List<UserBackground> findByBackgroundId(String backgroundId);

    /**
     * 특정 배경을 보유한 사용자 수 조회
     */
    long countByBackgroundId(String backgroundId);

    /**
     * 사용자가 보유한 배경 수 조회
     */
    long countByUserId(Long userId);

    /**
     * 특정 기간에 획득한 배경 조회
     */
    List<UserBackground> findByUserIdAndAcquiredAtBetween(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자별 최근 획득한 배경 조회
     */
    @Query("SELECT ub FROM UserBackground ub WHERE ub.userId = :userId " +
           "ORDER BY ub.acquiredAt DESC")
    List<UserBackground> findRecentAcquiredByUser(@Param("userId") Long userId, 
                                                 org.springframework.data.domain.Pageable pageable);

    /**
     * 특정 배경의 최근 보유자 조회
     */
    @Query("SELECT ub FROM UserBackground ub WHERE ub.backgroundId = :backgroundId " +
           "ORDER BY ub.acquiredAt DESC")
    List<UserBackground> findRecentOwnersByBackground(@Param("backgroundId") String backgroundId,
                                                     org.springframework.data.domain.Pageable pageable);

    /**
     * 사용자의 배경 보유 여부를 배경 ID 목록으로 한번에 조회
     */
    @Query("SELECT ub.backgroundId FROM UserBackground ub WHERE ub.userId = :userId " +
           "AND ub.backgroundId IN :backgroundIds")
    List<String> findOwnedBackgroundIds(@Param("userId") Long userId, 
                                       @Param("backgroundIds") List<String> backgroundIds);

    /**
     * 모든 사용자가 기본으로 보유해야 할 배경 확인
     */
    @Query("SELECT COUNT(ub) > 0 FROM UserBackground ub WHERE ub.userId = :userId " +
           "AND ub.backgroundId = 'bg_room_basic'")
    boolean hasDefaultBackground(@Param("userId") Long userId);
}
