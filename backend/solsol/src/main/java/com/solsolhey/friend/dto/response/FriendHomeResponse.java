package com.solsolhey.friend.dto.response;

import com.solsolhey.mascot.dto.view.MascotSummary;
import com.solsolhey.mascot.dto.view.Permissions;
import com.solsolhey.mascot.dto.view.ViewMode;
import lombok.Builder;

/**
 * 친구 홈 화면 응답 DTO
 */
@Builder
public record FriendHomeResponse(
        Long ownerId,
        String nickname,
        Integer level,
        Long likeCount,              // 누적 합
        Integer likeSentToday,       // 오늘 내가 그 친구에게 보낸 수
        Integer likeReceivedToday,   // 오늘 그 친구가 나에게 보낸 수
        Integer likeAllowedMax,      // 고정 1 (기본 1회, 콜백 1회까지 허용하되 남은 가능은 최대 1)
        Integer likeRemainingToday,  // clamp(1 + likeReceivedToday - likeSentToday, 0..1)
        Boolean canLikeNow,          // likeRemainingToday > 0
        ViewMode viewMode,
        Permissions permissions,
        MascotSummary mascot,
        Boolean isFriend,
        Boolean isOwner
) { }
