package com.pleasebookme.server.integration.oauthstate.store;

import com.pleasebookme.server.integration.oauthstate.model.OAuthFlowMode;
import com.pleasebookme.server.integration.oauthstate.model.OAuthState;
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
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisOAuthStateStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private RedisOAuthStateStore store;

    private static OAuthState sampleState() {
        return new OAuthState(
            OAuthFlowMode.CONNECT,
            UUID.randomUUID(),
            "verifier-abc",
            List.of("openid", "https://www.googleapis.com/auth/calendar"),
            "/dashboard/settings/integrations"
        );
    }

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisOAuthStateStore(redisTemplate, new ObjectMapper());
    }

    @Test
    void issue_storesUnderPrefixedKeyWithTtlAndNx() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(true);

        String token = store.issue(sampleState(), Duration.ofMinutes(10));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(valueOperations).setIfAbsent(
            keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofMinutes(10))
        );

        assertThat(keyCaptor.getValue()).isEqualTo("oauth:state:" + token);
        assertThat(valueCaptor.getValue()).contains("verifier-abc");
        // Opaque, high-entropy, URL-safe.
        assertThat(token).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void issue_whenKeyAlreadyExists_fails() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(false);

        assertThatThrownBy(() -> store.issue(sampleState(), Duration.ofMinutes(10)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void consume_roundTripsThePayloadAndUsesGetAndDelete() {
        OAuthState original = sampleState();
        String json = new ObjectMapper().writeValueAsString(original);

        when(valueOperations.getAndDelete("oauth:state:tok")).thenReturn(json);

        Optional<OAuthState> resolved = store.consume("tok");

        assertThat(resolved).isPresent();
        assertThat(resolved.get().mode()).isEqualTo(OAuthFlowMode.CONNECT);
        assertThat(resolved.get().userUid()).isEqualTo(original.userUid());
        assertThat(resolved.get().codeVerifier()).isEqualTo("verifier-abc");
        assertThat(resolved.get().requestedScopes()).containsExactlyElementsOf(original.requestedScopes());
        assertThat(resolved.get().redirectAfter()).isEqualTo("/dashboard/settings/integrations");

        // GETDEL, not GET: consumption must delete in the same round trip.
        verify(valueOperations).getAndDelete("oauth:state:tok");
    }

    @Test
    void consume_secondTimeReturnsEmpty_soAStateIsSingleUse() {
        when(valueOperations.getAndDelete("oauth:state:tok")).thenReturn(null);

        assertThat(store.consume("tok")).isEmpty();
    }

    @Test
    void consume_nullOrBlankState_returnsEmptyWithoutTouchingRedis() {
        assertThat(store.consume(null)).isEmpty();
        assertThat(store.consume("  ")).isEmpty();

        verify(valueOperations, org.mockito.Mockito.never()).getAndDelete(anyString());
    }

    @Test
    void consume_corruptPayload_returnsEmptyRatherThanThrowing() {
        when(valueOperations.getAndDelete("oauth:state:tok")).thenReturn("{not-json");

        assertThat(store.consume("tok")).isEmpty();
    }

    @Test
    void consume_payloadWrittenBeforeModeExisted_stillResolvesAsAConnect() {
        // A state issued by the previous deploy has no mode field. It must still
        // finish, which is why the callback branches on SIGN_UP_AND_CONNECT
        // rather than on CONNECT.
        String legacyJson = """
            {"userUid":"%s","codeVerifier":"verifier-abc",\
            "requestedScopes":["openid"],"redirectAfter":"/dashboard/settings/integrations"}\
            """.formatted(UUID.randomUUID());

        when(valueOperations.getAndDelete("oauth:state:tok")).thenReturn(legacyJson);

        Optional<OAuthState> resolved = store.consume("tok");

        assertThat(resolved).isPresent();
        assertThat(resolved.get().mode()).isNull();
        assertThat(resolved.get().userUid()).isNotNull();
    }
}
