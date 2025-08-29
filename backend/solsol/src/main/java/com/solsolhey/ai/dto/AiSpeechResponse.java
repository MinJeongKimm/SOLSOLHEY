package com.solsolhey.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSpeechResponse {
    private boolean success;
    private String message;
    private int expiresInSec;
}

