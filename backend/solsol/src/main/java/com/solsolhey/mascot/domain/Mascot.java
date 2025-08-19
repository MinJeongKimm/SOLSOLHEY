package com.solsolhey.mascot.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mascots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Mascot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "type", nullable = false, length = 20)
    private String type;
    
    @Column(name = "equipped_item", length = 100)
    private String equippedItem;
    
    @Column(name = "exp", nullable = false)
    @Builder.Default
    private Integer exp = 0;
    
    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 경험치 증가 및 레벨업 로직
    public void addExp(int expToAdd) {
        this.exp += expToAdd;
        updateLevel();
    }
    
    // 레벨 계산 (경험치 100당 1레벨)
    private void updateLevel() {
        this.level = (this.exp / 100) + 1;
    }
    
    // 아이템 장착
    public void equipItem(String item) {
        this.equippedItem = item;
    }
    
    // 이름 변경
    public void changeName(String newName) {
        this.name = newName;
    }
}
