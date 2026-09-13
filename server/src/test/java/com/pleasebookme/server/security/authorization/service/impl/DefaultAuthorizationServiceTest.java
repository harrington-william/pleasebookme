package com.pleasebookme.server.security.authorization.service.impl;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.exception.AuthorizationDeniedException;
import com.pleasebookme.server.security.authorization.exception.PolicyEvaluationException;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.authorization.registry.AuthorizationPolicyRegistry;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationServiceTest {

    @Mock
    private AuthorizationPolicyRegistry policyRegistry;

    private DefaultAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAuthorizationService(policyRegistry);
    }

    private static UserPrincipal principal() {
        return new UserPrincipal(
            AuthenticatedActorType.USER,
            UUID.randomUUID(), BigInteger.ONE, null,
            "jane", "jane@example.com", "Jane Doe", null,
            "Australia/Sydney", AccountStatus.ACTIVE, Set.of(), Set.of(), Map.of()
        );
    }

    private static AuthorizationContext context() {
        return new AuthorizationContext(principal(), "BOOKING", "CREATE", null, null, null, null);
    }

    private static AuthorizationPolicy policyReturning(AuthorizationDecision decision) {
        AuthorizationPolicy policy = mock(AuthorizationPolicy.class);
        when(policy.evaluate(any())).thenReturn(decision);
        return policy;
    }

    @Test
    void authorize_permits_whenASinglePolicyPermits() {
        AuthorizationPolicy policy = policyReturning(AuthorizationDecision.permit("P"));
        when(policyRegistry.resolve("BOOKING", "CREATE")).thenReturn(List.of(policy));

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.granted()).isTrue();
        assertThat(decision.policyName()).isEqualTo("P");
    }

    @Test
    void authorize_denies_whenEveryPolicyAbstains() {
        AuthorizationPolicy policy = policyReturning(AuthorizationDecision.abstain("P"));
        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(policy));

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.granted()).isFalse();
        assertThat(decision.code()).isEqualTo("NO_POLICY");
    }

    @Test
    void authorize_denies_whenNoPolicyIsRegisteredAtAll() {
        when(policyRegistry.resolve(any(), any())).thenReturn(List.of());

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.granted()).isFalse();
        assertThat(decision.code()).isEqualTo("NO_POLICY");
    }

    @Test
    void authorize_denyShortCircuits_laterPoliciesInTheOrderedListAreNeverEvaluated() {
        AuthorizationPolicy denying = policyReturning(
            AuthorizationDecision.deny("D", "SOME_CODE", "nope")
        );
        AuthorizationPolicy neverReached = mock(AuthorizationPolicy.class);

        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(denying, neverReached));

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.granted()).isFalse();
        assertThat(decision.code()).isEqualTo("SOME_CODE");
        verify(neverReached, never()).evaluate(any());
    }

    @Test
    void authorize_laterDenyOverridesAnEarlierPermit() {
        AuthorizationPolicy permitting = policyReturning(AuthorizationDecision.permit("P"));
        AuthorizationPolicy denying = policyReturning(
            AuthorizationDecision.deny("D", "LATER_DENY", "no")
        );

        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(permitting, denying));

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.granted()).isFalse();
        assertThat(decision.code()).isEqualTo("LATER_DENY");
    }

    @Test
    void authorize_firstPermitWins_whenMultiplePoliciesPermitAndNoneDeny() {
        AuthorizationPolicy first = policyReturning(AuthorizationDecision.permit("FIRST"));
        AuthorizationPolicy second = policyReturning(AuthorizationDecision.permit("SECOND"));

        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(first, second));

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.granted()).isTrue();
        assertThat(decision.policyName()).isEqualTo("FIRST");
    }

    @Test
    void authorize_neverReturnsAnAbstainEffectDirectly() {
        AuthorizationPolicy abstaining = policyReturning(AuthorizationDecision.abstain("P"));
        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(abstaining));

        AuthorizationDecision decision = service.authorize(context());

        assertThat(decision.effect()).isNotEqualTo(
            com.pleasebookme.server.security.authorization.decision.DecisionEffect.ABSTAIN
        );
    }

    @Test
    void authorize_wrapsAnUncheckedExceptionThrownByAPolicy() {
        AuthorizationPolicy broken = mock(AuthorizationPolicy.class);
        when(broken.evaluate(any())).thenThrow(new IllegalStateException("boom"));
        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(broken));

        assertThatThrownBy(() -> service.authorize(context()))
            .isInstanceOf(PolicyEvaluationException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void require_throwsAuthorizationDeniedException_whenNotGranted() {
        when(policyRegistry.resolve(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.require(context()))
            .isInstanceOf(AuthorizationDeniedException.class)
            .satisfies(exception -> {
                AuthorizationDeniedException denied = (AuthorizationDeniedException) exception;
                assertThat(denied.decision().code()).isEqualTo("NO_POLICY");
            });
    }

    @Test
    void require_doesNotThrow_whenGranted() {
        AuthorizationPolicy policy = policyReturning(AuthorizationDecision.permit("P"));
        when(policyRegistry.resolve(any(), any())).thenReturn(List.of(policy));

        assertThatCode(() -> service.require(context())).doesNotThrowAnyException();
    }

    @Test
    void authorize_looksUpPoliciesByTheContextsResourceTypeAndAction_notHardcoded() {
        AuthorizationContext customerContext =
            new AuthorizationContext(principal(), "CUSTOMER", "DELETE", null, null, null, null);

        when(policyRegistry.resolve(eq("CUSTOMER"), eq("DELETE"))).thenReturn(List.of());

        service.authorize(customerContext);

        verify(policyRegistry).resolve("CUSTOMER", "DELETE");
    }
}
