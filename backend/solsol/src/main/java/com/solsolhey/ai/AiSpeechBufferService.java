package com.solsolhey.ai;

import com.solsolhey.ai.dto.AiSpeechGenResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSpeechBufferService {
    private final AiMessageService aiMessageService;

    private static final int DEFAULT_PER_TYPE_TARGET = 2; // ACADEMIC 2 + CHALLENGE 2 = 4 total
    private static final long TTL_MILLIS = 180_000; // 180s
    private static final long IDLE_EVICT_MILLIS = 15 * 60_000; // 15m

    private final Map<Long, Deque<Entry>> academicQ = new ConcurrentHashMap<>();
    private final Map<Long, Deque<Entry>> challengeQ = new ConcurrentHashMap<>();
    private final Map<Long, String> lastServed = new ConcurrentHashMap<>(); // "ACADEMIC" or "CHALLENGE"
    private final Map<Long, Long> lastAccess = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AiSpeechGenResult getNext(Long userId) {
        long now = System.currentTimeMillis();
        lastAccess.put(userId, now);
        purgeExpired(userId, now);

        // Prefer strict alternation: flip last served type
        String prev = lastServed.getOrDefault(userId, "CHALLENGE");
        String desired = Objects.equals(prev, "ACADEMIC") ? "CHALLENGE" : "ACADEMIC";
        Entry e = pollByType(userId, desired);
        if (e == null) {
            // fallback to other type if desired empty
            String other = Objects.equals(desired, "ACADEMIC") ? "CHALLENGE" : "ACADEMIC";
            e = pollByType(userId, other);
        }
        if (e == null) {
            // Synchronous single generation as last resort
            var gen = aiMessageService.generateSpeech(userId);
            offer(userId, gen);
            e = pollByType(userId, desired);
            if (e == null) e = pollByType(userId, Objects.equals(desired, "ACADEMIC") ? "CHALLENGE" : "ACADEMIC");
        }
        // Refill in background toward target
        refillAsync(userId, DEFAULT_PER_TYPE_TARGET);
        if (e == null) {
            // absolute fallback (shouldn't happen)
            return new AiSpeechGenResult("오늘 할 일 하나 찜해볼까?", "CHALLENGE");
        }
        lastServed.put(userId, e.item.getKind());
        return e.item;
    }

    public void prefill(Long userId, int perTypeTarget) {
        long now = System.currentTimeMillis();
        lastAccess.put(userId, now);
        purgeExpired(userId, now);

        ensureTargetSync(userId, Math.max(1, perTypeTarget));
        // kick one more async to smooth burst
        refillAsync(userId, Math.max(1, perTypeTarget));
    }

    public void refillAsync(Long userId, int perTypeTarget) {
        executor.submit(() -> {
            try {
                ensureTargetSync(userId, perTypeTarget);
                evictIfIdle(userId, System.currentTimeMillis());
            } catch (Exception ex) {
                log.warn("buffer refill failed: {}", ex.toString());
            }
        });
    }

    private void ensureTargetSync(Long userId, int perTypeTarget) {
        // cap attempts to avoid runaway model calls
        int maxAttemptsPerType = perTypeTarget * 3;

        while (sizeOf(academicQ, userId) < perTypeTarget && maxAttemptsPerType-- > 0) {
            var gen = aiMessageService.generateSpeech(userId);
            offer(userId, gen);
        }
        maxAttemptsPerType = perTypeTarget * 3;
        while (sizeOf(challengeQ, userId) < perTypeTarget && maxAttemptsPerType-- > 0) {
            var gen = aiMessageService.generateSpeech(userId);
            offer(userId, gen);
        }
    }

    private Entry pollByType(Long userId, String type) {
        if (Objects.equals(type, "ACADEMIC")) {
            return pollNonExpired(academicQ.computeIfAbsent(userId, k -> new ArrayDeque<>()));
        } else if (Objects.equals(type, "CHALLENGE")) {
            return pollNonExpired(challengeQ.computeIfAbsent(userId, k -> new ArrayDeque<>()));
        }
        return null;
    }

    private void offer(Long userId, AiSpeechGenResult gen) {
        String kind = gen.getKind();
        long exp = System.currentTimeMillis() + TTL_MILLIS;
        if (Objects.equals(kind, "ACADEMIC")) {
            academicQ.computeIfAbsent(userId, k -> new ArrayDeque<>()).offerLast(new Entry(gen, exp));
        } else if (Objects.equals(kind, "CHALLENGE")) {
            challengeQ.computeIfAbsent(userId, k -> new ArrayDeque<>()).offerLast(new Entry(gen, exp));
        } else {
            // default bucket
            academicQ.computeIfAbsent(userId, k -> new ArrayDeque<>()).offerLast(new Entry(gen, exp));
        }
    }

    private void offerBack(Long userId, Entry entry) {
        if (entry == null) return;
        if (Objects.equals(entry.item.getKind(), "ACADEMIC")) {
            academicQ.computeIfAbsent(userId, k -> new ArrayDeque<>()).offerFirst(entry);
        } else {
            challengeQ.computeIfAbsent(userId, k -> new ArrayDeque<>()).offerFirst(entry);
        }
    }

    private void purgeExpired(Long userId, long now) {
        removeExpired(academicQ.computeIfAbsent(userId, k -> new ArrayDeque<>()), now);
        removeExpired(challengeQ.computeIfAbsent(userId, k -> new ArrayDeque<>()), now);
    }

    private void evictIfIdle(Long userId, long now) {
        Long last = lastAccess.get(userId);
        if (last != null && now - last > IDLE_EVICT_MILLIS) {
            academicQ.remove(userId);
            challengeQ.remove(userId);
            lastAccess.remove(userId);
        }
    }

    private int sizeOf(Map<Long, Deque<Entry>> map, Long userId) {
        return map.computeIfAbsent(userId, k -> new ArrayDeque<>()).size();
    }

    private Entry pollNonExpired(Deque<Entry> dq) {
        long now = System.currentTimeMillis();
        while (!dq.isEmpty()) {
            Entry e = dq.pollFirst();
            if (e.expiresAt > now) return e;
        }
        return null;
    }

    private void removeExpired(Deque<Entry> dq, long now) {
        dq.removeIf(e -> e.expiresAt <= now);
    }

    private record Entry(AiSpeechGenResult item, long expiresAt) {}
}
