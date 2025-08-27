package com.solsolhey.user.repository;

import com.solsolhey.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 엔티티 레포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // username 기반 조회는 더 이상 사용하지 않습니다 (principal은 이메일 사용).

    /**
     * 사용자 ID로 활성 사용자 조회
     */
    Optional<User> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 이메일로 활성 사용자 조회
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    // username 기반 조회는 더 이상 사용하지 않습니다.

    /**
     * 이메일로 사용자 조회 (활성/비활성 무관)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회 (활성/비활성 무관)
     */
    List<User> findByNickname(String nickname);

    /**
     * 닉네임 중복 여부
     */
    boolean existsByNickname(String nickname);

    // username 중복 체크는 더 이상 사용하지 않습니다.

    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);

    /**
     * 캠퍼스별 활성 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.campus = :campus AND u.isActive = true")
    Long countActiveByCampus(@Param("campus") String campus);

    /**
     * 포인트 상위 사용자 조회 (캠퍼스별)
     */
    @Query("SELECT u FROM User u WHERE u.campus = :campus AND u.isActive = true " +
           "ORDER BY u.totalPoints DESC")
    java.util.List<User> findTopUsersByCampusOrderByTotalPointsDesc(
            @Param("campus") String campus, 
            org.springframework.data.domain.Pageable pageable);

    /**
     * 포인트 상위 사용자 조회 (전체)
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.totalPoints DESC")
    java.util.List<User> findTopUsersOrderByTotalPointsDesc(
            org.springframework.data.domain.Pageable pageable);

    // username 포함 검색은 사용하지 않습니다. 필요 시 닉네임/이메일 기준으로 별도 구현.
    
}
