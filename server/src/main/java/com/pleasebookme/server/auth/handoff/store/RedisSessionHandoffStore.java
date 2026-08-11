package com.pleasebookme.server.auth.handoff.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionHandoffStore implements SessionHandoffStore {
    private static final String KEY_PREFIX = "oauth:handoff:";
    private static final int CODE_BYTES = 32;

    private final StringRedisTemplate redisTemplate;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String issue(
        UUID userUid,
        Duration ttl
    ) {
        byte[] bytes = new byte[CODE_BYTES];
        secureRandom.nextBytes(bytes);

        String code = encoder.encodeToString(bytes);

        // SetIfAbsent guarantees a new code can never clobber a live one
        Boolean stored = redisTemplate
            .opsForValue()
            .setIfAbsent(KEY_PREFIX + code, userUid.toString(), ttl);

        if (!Boolean.TRUE.equals(stored)) {
            throw new IllegalStateException("Failed to store session handoff in Redis");
        }

        return code;
    }

    @Override
    public Optional<UUID> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        // GETDEL: Handoff codes are single-use
        String payload = redisTemplate
            .opsForValue()
            .getAndDelete(KEY_PREFIX + code);

        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(payload));
        } catch (IllegalArgumentException exception) {
            log.warn("Discarding unreadable session handoff payload: {}", exception.getMessage());
            return Optional.empty();
        }
    }
}
