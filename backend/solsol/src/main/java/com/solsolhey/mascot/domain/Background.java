package com.solsolhey.mascot.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마스코트 배경 정보 엔티티
 */
@Entity
@Table(name = "backgrounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Background {
    
    @Id
    @Column(name = "id", length = 50)
    private String id; // 예: "bg_room_basic"
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "preview_url", nullable = false, length = 500)
    private String previewUrl;
    
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    @ElementCollection
    @Column(name = "tag")
    private List<String> tags;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 배경 활성화
     */
    public void enable() {
        this.enabled = true;
    }
    
    /**
     * 배경 비활성화
     */
    public void disable() {
        this.enabled = false;
    }
    
    /**
     * 배경 정보 업데이트
     */
    public void updateInfo(String name, String previewUrl, List<String> tags) {
        if (name != null) this.name = name;
        if (previewUrl != null) this.previewUrl = previewUrl;
        if (tags != null) this.tags = tags;
    }
}
