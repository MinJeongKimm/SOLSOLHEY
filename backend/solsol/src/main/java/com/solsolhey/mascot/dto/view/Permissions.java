package com.solsolhey.mascot.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permissions {
    private boolean canEdit;
    private boolean canEquip;
    private boolean canSendFriendRequest;
    private boolean canCheer;

    public static Permissions self() {
        return Permissions.builder()
                .canEdit(true)
                .canEquip(true)
                .canSendFriendRequest(false)
                .canCheer(false)
                .build();
    }

    public static Permissions other() {
        return Permissions.builder()
                .canEdit(false)
                .canEquip(false)
                .canSendFriendRequest(false)
                .canCheer(false)
                .build();
    }

    public static Permissions loggedInVisitor() {
        // TODO: 실제 친구 관계를 확인하여 친구 신청/응원하기 여부를 결정해야 합니다.
        return Permissions.builder()
                .canEdit(false)
                .canEquip(false)
                .canSendFriendRequest(true)
                .canCheer(true)
                .build();
    }

    public static Permissions publicViewer() {
        // 공개 조회자 (로그인하지 않은 사용자)는 모든 상호작용 불가
        return Permissions.builder()
                .canEdit(false)
                .canEquip(false)
                .canSendFriendRequest(false)
                .canCheer(false)
                .build();
    }
}

