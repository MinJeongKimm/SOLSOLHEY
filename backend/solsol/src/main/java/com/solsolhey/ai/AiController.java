package com.solsolhey.ai;

import com.solsolhey.ai.dto.AiSpeechResponse;
import com.solsolhey.auth.dto.response.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiMessageService aiMessageService;

    @PostMapping("/speech")
    public ResponseEntity<Map<String, Object>> generateSpeech(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        String msg = aiMessageService.generateSpeech(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.TRUE);
        body.put("message", msg);
        body.put("expiresInSec", Integer.valueOf(10));
        return ResponseEntity.ok(body);
    }
}
