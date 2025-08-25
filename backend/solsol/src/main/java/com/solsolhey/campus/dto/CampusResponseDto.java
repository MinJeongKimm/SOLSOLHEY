package com.solsolhey.campus.dto;

import com.solsolhey.campus.Campus;
import lombok.Getter;

@Getter
public class CampusResponseDto {

    private final Long id;
    private final String name;

    public CampusResponseDto(Campus campus) {
        this.id = campus.getId();
        this.name = campus.getName();
    }
}
