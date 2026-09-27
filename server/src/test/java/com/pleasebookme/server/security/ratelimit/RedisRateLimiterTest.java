package com.pleasebookme.server.security.ratelimit;

import com.pleasebookme.server.security.ratelimit.engine.RedisRateLimiter;
import com.pleasebookme.server.security.ratelimit.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimiterTest {
    @Mock private StringRedisTemplate redisTemplate;

    private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RedisRateLimiter(redisTemplate);
    }

    @Test
    void check_executesOneAtomicScriptWithPrefixedKeyAndWindow() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
            .thenReturn(1L);

        assertThatCode(() -> rateLimiter.check(
            "write",
            "1.2.3.4",
            5,
            Duration.ofSeconds(60)
        )).doesNotThrowAnyException();

        ArgumentCaptor<List<String>> keys = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> seconds = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).execute(
            any(RedisScript.class),
            keys.capture(),
            seconds.capture()
        );
        assertThat(keys.getValue()).containsExactly("ratelimit:public:write:1.2.3.4");
        assertThat(seconds.getValue()).isEqualTo("60");
    }

    @Test
    void check_whenLimitExceeded_usesRedisTtl() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
            .thenReturn(6L);
        when(redisTemplate.getExpire("ratelimit:public:write:1.2.3.4"))
            .thenReturn(27L);

        assertThatThrownBy(() -> rateLimiter.check(
            "write",
            "1.2.3.4",
            5,
            Duration.ofSeconds(60)
        )).isInstanceOfSatisfying(
            RateLimitExceededException.class,
            exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(27)
        );
    }

    @Test
    void check_whenTtlUnavailable_fallsBackToWindow() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
            .thenReturn(6L);
        when(redisTemplate.getExpire("ratelimit:public:write:1.2.3.4"))
            .thenReturn(-1L);

        assertThatThrownBy(() -> rateLimiter.check(
            "write",
            "1.2.3.4",
            5,
            Duration.ofSeconds(60)
        )).isInstanceOfSatisfying(
            RateLimitExceededException.class,
            exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(60)
        );
    }

    @Test
    void check_whenRedisFails_propagatesFailure() {
        RedisConnectionFailureException failure =
            new RedisConnectionFailureException("unavailable");
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
            .thenThrow(failure);

        assertThatThrownBy(() -> rateLimiter.check(
            "read",
            "1.2.3.4",
            120,
            Duration.ofMinutes(1)
        )).isSameAs(failure);
    }
}
