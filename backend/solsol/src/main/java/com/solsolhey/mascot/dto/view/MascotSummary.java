package com.solsolhey.mascot.dto.view;

import com.solsolhey.mascot.dto.MascotCustomizationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotSummary {
    private String name;
    private String type;
    private String equippedItem;
    private Integer level;
    private String backgroundColor;
    private String backgroundPattern;
    
    // 장착된 아이템 목록 (커스터마이징용)
    private List<MascotCustomizationDto.Item> equippedItems;
    
    // 마스코트 커스터마이징 레이아웃 (JSON 문자열)
    private String equippedLayout;
}

