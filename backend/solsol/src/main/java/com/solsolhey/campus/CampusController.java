package com.solsolhey.campus;

import com.solsolhey.campus.dto.CampusResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campus")
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    public ResponseEntity<List<CampusResponseDto>> getCampusList() {
        List<CampusResponseDto> campusList = campusService.getCampusList().stream()
                .map(CampusResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(campusList);
    }
}
