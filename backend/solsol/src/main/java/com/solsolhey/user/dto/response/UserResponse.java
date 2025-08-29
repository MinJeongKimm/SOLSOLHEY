package com.solsolhey.user.dto.response;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
public record UserResponse(
    Long userId,
    String email,
    String nickname,
    String campus,
    Integer totalPoints,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
