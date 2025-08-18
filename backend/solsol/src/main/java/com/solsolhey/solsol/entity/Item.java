package com.solsolhey.solsol.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아이템 엔티티 (마스코트 꾸미기용)
 */
@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, length = 20)
    private ItemRarity rarity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public Item(String itemName, ItemType itemType, Integer price, 
                String description, String imageUrl, ItemRarity rarity) {
        this.itemName = itemName;
        this.itemType = itemType;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rarity = rarity;
        this.isActive = true;
    }

    /**
     * 아이템 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 아이템 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 가격 변경
     */
    public void updatePrice(Integer newPrice) {
        if (newPrice >= 0) {
            this.price = newPrice;
        }
    }

    /**
     * 아이템 타입 열거형
     */
    public enum ItemType {
        CLOTHING("의상"),
        BACKGROUND("배경"),
        ACCESSORY("악세서리");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 아이템 희귀도 열거형
     */
    public enum ItemRarity {
        COMMON("일반", "#808080"),
        RARE("레어", "#0080FF"),
        EPIC("에픽", "#8000FF"),
        LEGENDARY("전설", "#FF8000");

        private final String displayName;
        private final String colorCode;

        ItemRarity(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }
    }
}
