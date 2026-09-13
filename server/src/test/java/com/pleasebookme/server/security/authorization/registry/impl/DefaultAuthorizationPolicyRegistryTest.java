package com.pleasebookme.server.security.authorization.registry.impl;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAuthorizationPolicyRegistryTest {

    private record FakePolicy(
        String resourceType,
        Predicate<String> actionSupport,
        int order
    ) implements AuthorizationPolicy {

        @Override
        public String resourceType() {
            return resourceType;
        }

        @Override
        public boolean supports(String action) {
            return actionSupport.test(action);
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public AuthorizationDecision evaluate(AuthorizationContext context) {
            throw new UnsupportedOperationException("not exercised by registry tests");
        }
    }

    @Test
    void resolve_returnsWildcardPolicy_whenNoResourceSpecificPolicyIsRegistered() {
        FakePolicy wildcard = new FakePolicy("*", action -> true, -20);
        DefaultAuthorizationPolicyRegistry registry = registryOf(wildcard);

        assertThat(registry.resolve("BOOKING", "CREATE")).containsExactly(wildcard);
    }

    @Test
    void resolve_mergesWildcardAndResourceSpecificPolicies_sortedByOrderRegardlessOfInjectionOrder() {
        FakePolicy wildcardLow = new FakePolicy("*", action -> true, -20);
        FakePolicy wildcardHigh = new FakePolicy("*", action -> true, 0);
        FakePolicy specific = new FakePolicy("BOOKING", action -> true, 10);

        // Deliberately scrambled injection order to prove the registry sorts
        // rather than relying on Spring's bean-list ordering.
        DefaultAuthorizationPolicyRegistry registry = registryOf(specific, wildcardHigh, wildcardLow);

        assertThat(registry.resolve("BOOKING", "CREATE"))
            .containsExactly(wildcardLow, wildcardHigh, specific);
    }

    @Test
    void resolve_excludesPoliciesThatDoNotSupportTheRequestedAction() {
        FakePolicy createOnly = new FakePolicy("BOOKING", "CREATE"::equals, 10);
        FakePolicy updateOnly = new FakePolicy("BOOKING", "UPDATE"::equals, 10);
        DefaultAuthorizationPolicyRegistry registry = registryOf(createOnly, updateOnly);

        assertThat(registry.resolve("BOOKING", "CREATE")).containsExactly(createOnly);
        assertThat(registry.resolve("BOOKING", "UPDATE")).containsExactly(updateOnly);
    }

    @Test
    void resolve_isolatesPoliciesRegisteredForADifferentResourceType() {
        FakePolicy booking = new FakePolicy("BOOKING", action -> true, 10);
        FakePolicy customer = new FakePolicy("CUSTOMER", action -> true, 10);
        DefaultAuthorizationPolicyRegistry registry = registryOf(booking, customer);

        assertThat(registry.resolve("BOOKING", "CREATE")).containsExactly(booking);
        assertThat(registry.resolve("CUSTOMER", "CREATE")).containsExactly(customer);
    }

    @Test
    void resolve_returnsEmptyList_whenNoPolicyIsRegisteredAtAll() {
        DefaultAuthorizationPolicyRegistry registry = registryOf();

        assertThat(registry.resolve("BOOKING", "CREATE")).isEmpty();
    }

    @Test
    void resolve_doesNotDoubleCountWildcardPolicies_whenResourceTypeItselfIsWildcard() {
        FakePolicy wildcard = new FakePolicy("*", action -> true, -20);
        DefaultAuthorizationPolicyRegistry registry = registryOf(wildcard);

        // Without the explicit "*".equals(resourceType) guard in resolve(),
        // this would return [wildcard, wildcard] since indexing also stores
        // the wildcard bucket under byResourceType.get("*").
        assertThat(registry.resolve("*", "ANYTHING")).containsExactly(wildcard);
    }

    private DefaultAuthorizationPolicyRegistry registryOf(AuthorizationPolicy... policies) {
        DefaultAuthorizationPolicyRegistry registry = new DefaultAuthorizationPolicyRegistry(List.of(policies));
        registry.index();
        return registry;
    }
}
