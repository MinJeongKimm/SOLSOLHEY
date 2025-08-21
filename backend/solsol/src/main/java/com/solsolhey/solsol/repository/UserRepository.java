package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 엔티티 레포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명으로 활성 사용자 조회
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);

    /**
     * 사용자 ID로 활성 사용자 조회
     */
    Optional<User> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 이메일로 활성 사용자 조회
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * 사용자명으로 사용자 조회 (활성/비활성 무관)
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자 조회 (활성/비활성 무관)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회 (활성/비활성 무관)
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 사용자명 중복 체크
     */
    boolean existsByUsername(String username);

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

    /**
     * 사용자명이나 닉네임으로 검색 (대소문자 무관)
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%')))")
    java.util.List<User> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(
            @Param("username") String username, @Param("nickname") String nickname);
}
