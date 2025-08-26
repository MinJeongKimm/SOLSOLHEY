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
}

