package com.solsolhey.mascot.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.mascot.dto.view.MascotViewResponse;
import com.solsolhey.mascot.service.MascotViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mascot")
@RequiredArgsConstructor
@Slf4j
public class MascotViewController {

    private final MascotViewService mascotViewService;

    @GetMapping("/view")
    public ResponseEntity<ApiResponse<MascotViewResponse>> getView(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("ownerId") Long ownerId
    ) {
        Long viewerId = userDetails != null ? userDetails.getUserId() : null;
        log.info("마스코트 뷰 조회: viewerId={}, ownerId={}", viewerId, ownerId);
        MascotViewResponse response = mascotViewService.getView(viewerId, ownerId);
        return ResponseEntity.ok(ApiResponse.success("마스코트 뷰 조회 완료", response));
    }
}

