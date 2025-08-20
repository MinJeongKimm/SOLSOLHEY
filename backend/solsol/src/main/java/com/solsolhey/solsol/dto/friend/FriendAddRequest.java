package com.solsolhey.solsol.dto.friend;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
public class FriendAddRequest {

    private String code;     // 초대 코드
    private Long userId;     // 사용자 ID
    private String username; // 사용자명

    public FriendAddRequest(String code) {
        this.code = code;
    }

    public FriendAddRequest(Long userId) {
        this.userId = userId;
    }

    public FriendAddRequest(String username, boolean isUsername) {
        this.username = username;
    }

    /**
     * 유효성 검증
     */
    public void validate() {
        if (code == null && userId == null && username == null) {
            throw new IllegalArgumentException("code, userId, username 중 하나는 필수입니다.");
        }
        
        int nonNullCount = 0;
        if (code != null) nonNullCount++;
        if (userId != null) nonNullCount++;
        if (username != null) nonNullCount++;
        
        if (nonNullCount > 1) {
            throw new IllegalArgumentException("code, userId, username 중 하나만 입력해주세요.");
        }
    }
}
