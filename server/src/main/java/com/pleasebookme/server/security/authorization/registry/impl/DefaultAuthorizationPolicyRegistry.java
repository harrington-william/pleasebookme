package com.pleasebookme.server.security.authorization.registry.impl;

import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.authorization.registry.AuthorizationPolicyRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultAuthorizationPolicyRegistry implements AuthorizationPolicyRegistry {

    private final List<AuthorizationPolicy> policies;

    private Map<String, List<AuthorizationPolicy>> byResourceType = Map.of();
    private List<AuthorizationPolicy> wildcardPolicies = List.of();

    @PostConstruct
    void index() {
        Map<String, List<AuthorizationPolicy>> grouped = new HashMap<>();

        for (AuthorizationPolicy policy : policies) {
            grouped
                .computeIfAbsent(policy.resourceType(), key -> new ArrayList<>())
                .add(policy);
        }

        for (List<AuthorizationPolicy> group : grouped.values()) {
            group.sort(Comparator.comparingInt(AuthorizationPolicy::order));
        }

        wildcardPolicies = List.copyOf(
            grouped.getOrDefault(AuthorizationPolicy.WILDCARD_RESOURCE_TYPE, List.of())
        );

        Map<String, List<AuthorizationPolicy>> indexed = new HashMap<>();

        grouped.forEach((resourceType, group) ->
            indexed.put(resourceType, List.copyOf(group)));

        byResourceType = Map.copyOf(indexed);
    }

    @Override
    public List<AuthorizationPolicy> resolve(String resourceType, String action) {
        List<AuthorizationPolicy> specific = AuthorizationPolicy.WILDCARD_RESOURCE_TYPE.equals(resourceType)
            ? List.of()
            : byResourceType.getOrDefault(resourceType, List.of());

        List<AuthorizationPolicy> combined = new ArrayList<>(wildcardPolicies.size() + specific.size());

        combined.addAll(wildcardPolicies);
        combined.addAll(specific);
        combined.removeIf(policy -> !policy.supports(action));
        combined.sort(Comparator.comparingInt(AuthorizationPolicy::order));

        return List.copyOf(combined);
    }
}
