package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.FriendInvitation;
import com.solsolhey.solsol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 친구 초대 Repository
 */
@Repository
public interface FriendInvitationRepository extends JpaRepository<FriendInvitation, Long> {

    /**
     * 초대 코드로 초대 정보 조회
     */
    Optional<FriendInvitation> findByInvitationCode(String invitationCode);

    /**
     * 사용자의 초대 목록 조회 (최신순)
     */
    List<FriendInvitation> findByInviterOrderByCreatedAtDesc(User inviter);

    /**
     * 사용자의 활성 초대 목록 조회
     */
    List<FriendInvitation> findByInviterAndStatusOrderByCreatedAtDesc(
            User inviter, FriendInvitation.InvitationStatus status);

    /**
     * 만료된 초대들 조회
     */
    @Query("SELECT fi FROM FriendInvitation fi WHERE fi.expiresAt < :now AND fi.status = 'PENDING'")
    List<FriendInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);

    /**
     * 특정 사용자가 수락한 초대들 조회
     */
    List<FriendInvitation> findByInvitedUserOrderByUsedAtDesc(User invitedUser);

    /**
     * 사용자별 초대 통계 - 총 생성한 초대 수
     */
    long countByInviter(User inviter);

    /**
     * 사용자별 초대 통계 - 수락된 초대 수
     */
    long countByInviterAndStatus(User inviter, FriendInvitation.InvitationStatus status);

    /**
     * 초대 코드 존재 여부 확인
     */
    boolean existsByInvitationCode(String invitationCode);

    /**
     * 특정 기간 내 생성된 초대들 조회
     */
    @Query("SELECT fi FROM FriendInvitation fi WHERE fi.inviter = :inviter " +
           "AND fi.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY fi.createdAt DESC")
    List<FriendInvitation> findByInviterAndCreatedAtBetween(
            @Param("inviter") User inviter, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
}
