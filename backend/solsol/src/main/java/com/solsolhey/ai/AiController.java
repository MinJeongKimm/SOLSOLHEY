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
    private final AiSpeechBufferService aiSpeechBufferService;

    @PostMapping("/speech")
    public ResponseEntity<Map<String, Object>> generateSpeech(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        var res = aiMessageService.generateSpeech(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.TRUE);
        body.put("message", res.getMessage());
        body.put("kind", res.getKind());
        body.put("expiresInSec", Integer.valueOf(10));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/speech/next")
    public ResponseEntity<Map<String, Object>> nextBuffered(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        var res = aiSpeechBufferService.getNext(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.TRUE);
        body.put("message", res.getMessage());
        body.put("kind", res.getKind());
        body.put("expiresInSec", Integer.valueOf(10));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/speech/prefill")
    public ResponseEntity<Map<String, Object>> prefill(@AuthenticationPrincipal CustomUserDetails user,
                                                       @RequestParam(name = "perType", required = false, defaultValue = "2") int perType) {
        Long userId = user.getUserId();
        aiSpeechBufferService.prefill(userId, Math.max(1, Math.min(perType, 6)));
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.TRUE);
        body.put("message", "prefilled");
        return ResponseEntity.ok(body);
    }
}
