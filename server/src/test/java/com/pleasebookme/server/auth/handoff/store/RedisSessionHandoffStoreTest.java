package com.pleasebookme.server.auth.handoff.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisSessionHandoffStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private RedisSessionHandoffStore store;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisSessionHandoffStore(redisTemplate);
    }

    @Test
    void issue_storesOnlyTheUserReferenceUnderAPrefixedKeyWithTtl() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(true);

        UUID userUid = UUID.randomUUID();
        String code = store.issue(userUid, Duration.ofSeconds(60));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(valueOperations).setIfAbsent(
            keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofSeconds(60))
        );

        assertThat(keyCaptor.getValue()).isEqualTo("oauth:handoff:" + code);
        // Never the tokens: this code travels in a query string and therefore
        // into browser history.
        assertThat(valueCaptor.getValue()).isEqualTo(userUid.toString());
        // Opaque, high-entropy, URL-safe.
        assertThat(code).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void issue_whenKeyAlreadyExists_fails() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(false);

        assertThatThrownBy(() -> store.issue(UUID.randomUUID(), Duration.ofSeconds(60)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void consume_returnsTheUserAndUsesGetAndDelete() {
        UUID userUid = UUID.randomUUID();
        when(valueOperations.getAndDelete("oauth:handoff:code")).thenReturn(userUid.toString());

        assertThat(store.consume("code")).contains(userUid);

        // GETDEL, not GET: consumption must delete in the same round trip.
        verify(valueOperations).getAndDelete("oauth:handoff:code");
    }

    @Test
    void consume_secondTimeReturnsEmpty_soACodeIsSingleUse() {
        when(valueOperations.getAndDelete("oauth:handoff:code")).thenReturn(null);

        assertThat(store.consume("code")).isEmpty();
    }

    @Test
    void consume_nullOrBlankCode_returnsEmptyWithoutTouchingRedis() {
        assertThat(store.consume(null)).isEmpty();
        assertThat(store.consume("  ")).isEmpty();

        verify(valueOperations, never()).getAndDelete(anyString());
    }

    @Test
    void consume_corruptPayload_returnsEmptyRatherThanThrowing() {
        when(valueOperations.getAndDelete("oauth:handoff:code")).thenReturn("not-a-uuid");

        assertThat(store.consume("code")).isEmpty();
    }
}
