package com.pleasebookme.server.security.ratelimit.engine;

import com.pleasebookme.server.security.ratelimit.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {
    private static final String KEY_PREFIX = "ratelimit:public:";
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT =
        new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]) " +
                "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                "return current",
            Long.class
        );

    private final StringRedisTemplate redisTemplate;

    @Override
    public void check(
        String bucket,
        String key,
        int limit,
        Duration window
    ) {
        String redisKey = KEY_PREFIX + bucket + ":" + key;
        long windowSeconds = window.toSeconds();

        // Lua keeps the increment and expiry atomic; Redis failures propagate so
        // this anonymous boundary fails closed instead of bypassing protection.
        Long current = redisTemplate.execute(
            INCREMENT_SCRIPT,
            List.of(redisKey),
            Long.toString(windowSeconds)
        );

        if (current != null && current > limit) {
            Long ttl = redisTemplate.getExpire(redisKey);
            long retryAfter = ttl == null || ttl < 0 ? windowSeconds : ttl;
            throw new RateLimitExceededException(retryAfter);
        }
    }
}
