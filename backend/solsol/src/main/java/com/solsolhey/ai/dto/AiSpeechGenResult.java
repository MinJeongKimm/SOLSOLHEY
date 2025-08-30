package com.solsolhey.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AiSpeechGenResult {
    private final String message;
    private final String kind; // "ACADEMIC" or "CHALLENGE"
}

