package com.solsolhey.solsol.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 초대 링크/코드를 관리하는 엔티티
 */
@Entity
@Table(name = "friend_invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter; // 초대한 사용자

    @Column(name = "invitation_code", unique = true, nullable = false, length = 100)
    private String invitationCode; // 초대 코드 (UUID 기반)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 만료 시간

    @Column(name = "used_at")
    private LocalDateTime usedAt; // 사용된 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private User invitedUser; // 초대를 수락한 사용자

    @Builder
    public FriendInvitation(User inviter, LocalDateTime expiresAt) {
        this.inviter = inviter;
        this.invitationCode = UUID.randomUUID().toString();
        this.expiresAt = expiresAt;
        this.status = InvitationStatus.PENDING;
    }

    /**
     * 초대 상태
     */
    public enum InvitationStatus {
        PENDING,  // 대기 중
        ACCEPTED, // 수락됨
        EXPIRED   // 만료됨
    }

    /**
     * 초대 수락 처리
     */
    public void accept(User invitedUser) {
        if (this.status != InvitationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 초대입니다.");
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = InvitationStatus.EXPIRED;
            throw new IllegalStateException("만료된 초대입니다.");
        }
        
        this.invitedUser = invitedUser;
        this.status = InvitationStatus.ACCEPTED;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 만료 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt) || this.status == InvitationStatus.EXPIRED;
    }

    /**
     * 사용 가능 확인
     */
    public boolean isUsable() {
        return this.status == InvitationStatus.PENDING && !isExpired();
    }
}
