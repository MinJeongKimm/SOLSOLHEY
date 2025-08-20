package com.solsolhey.shop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_user_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @CreatedDate
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;
    
    // 수량 추가
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
    
    // 수량 차감
    public void deductQuantity(int amount) {
        if (this.quantity < amount) {
            throw new IllegalStateException("보유 수량이 부족합니다. 현재 수량: " + this.quantity + ", 필요 수량: " + amount);
        }
        this.quantity -= amount;
    }
}

