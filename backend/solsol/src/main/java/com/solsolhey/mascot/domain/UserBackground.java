package com.solsolhey.mascot.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 보유 배경 정보 엔티티
 */
@Entity
@Table(name = "user_backgrounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserBackground {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "background_id", nullable = false, length = 50)
    private String backgroundId;
    
    @CreatedDate
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;
    
    /**
     * 사용자가 특정 배경을 보유하고 있는지 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
    
    /**
     * 특정 배경인지 확인
     */
    public boolean isBackground(String backgroundId) {
        return this.backgroundId.equals(backgroundId);
    }
}
