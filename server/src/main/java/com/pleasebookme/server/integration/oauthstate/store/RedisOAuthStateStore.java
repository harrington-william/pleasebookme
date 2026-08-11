package com.pleasebookme.server.integration.oauthstate.store;

import com.pleasebookme.server.integration.oauthstate.model.OAuthState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOAuthStateStore implements OAuthStateStore {
    private static final String KEY_PREFIX = "oauth:state:";
    private static final int STATE_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String issue(
        OAuthState state,
        Duration ttl
    ) {
        byte[] bytes = new byte[STATE_BYTES];
        secureRandom.nextBytes(bytes);

        // Generate a unique token to attach to Redis key
        String token = encoder.encodeToString(bytes);

        // SetIfAbsent guarantees an issuing state can never clobber a live one
        Boolean stored = redisTemplate
            .opsForValue()
            .setIfAbsent(KEY_PREFIX + token, objectMapper.writeValueAsString(state), ttl);

        if (!Boolean.TRUE.equals(stored)) {
            throw new IllegalStateException("Failed to store OAuth state in Redis");
        }

        return token;
    }

    @Override
    public Optional<OAuthState> consume(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }

        // GETDEL: States are single-use
        String payload = redisTemplate
            .opsForValue()
            .getAndDelete(KEY_PREFIX + state);

        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(payload, OAuthState.class));
        } catch (RuntimeException exception) {
            log.warn("Discarding unreadable OAuth state payload: {}", exception.getMessage());
            return Optional.empty();
        }
    }
}
