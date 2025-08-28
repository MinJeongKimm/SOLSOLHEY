package com.solsolhey.exp.controller;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.exp.entity.ExpDailyCounter;
import com.solsolhey.exp.repository.ExpDailyCounterRepository;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/exp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "EXP API", description = "경험치 요약 조회 API")
public class ExpController {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ExpDailyCounterRepository counterRepository;
    private final MascotRepository mascotRepository;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "EXP 요약", description = "총 EXP/레벨 및 오늘 상태 요약을 반환합니다.")
    public ApiResponse<ExpSummaryResponse> getSummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        var mascotOpt = mascotRepository.findByUserId(user.getUserId());
        int totalExp = mascotOpt.map(Mascot::getExp).orElse(0);
        int level = mascotOpt.map(Mascot::getLevel).orElse(1);

        LocalDate todayKst = LocalDate.now(KST);
        Optional<ExpDailyCounter> counterOpt = counterRepository.findByUserAndCounterDate(user, todayKst);

        Today today = counterOpt.map(c -> new Today(
                        c.getAttendanceAwarded(),
                        c.getAttendanceStreak7Awarded(),
                        new Friend(new FriendActive(c.getFriendActiveAwardCount(), Math.max(0, 3 - c.getFriendActiveAwardCount())),
                                   new FriendPassive(c.getFriendPassiveAwardCount())),
                        new Categories(c.getCatFinanceAwarded(), c.getCatAcademicAwarded(), c.getCatSocialAwarded(), c.getCatEventAwarded())
                ))
                .orElse(new Today(false, false,
                        new Friend(new FriendActive(0, 3), new FriendPassive(0)),
                        new Categories(false, false, false, false)));

        ExpSummaryResponse body = new ExpSummaryResponse(totalExp, level, today);
        return ApiResponse.success("OK", body);
    }

    // DTOs
    public record ExpSummaryResponse(int totalExp, int level, Today today) {}
    public record Today(boolean attendance, boolean streak7, Friend friend, Categories categories) {}
    public record Friend(FriendActive active, FriendPassive passive) {}
    public record FriendActive(int count, int remainingTop3) {}
    public record FriendPassive(int count) {}
    public record Categories(boolean FINANCE, boolean ACADEMIC, boolean SOCIAL, boolean EVENT) {}
}

