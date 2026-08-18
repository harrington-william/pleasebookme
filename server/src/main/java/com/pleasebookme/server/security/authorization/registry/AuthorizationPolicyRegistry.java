package com.pleasebookme.server.security.authorization.registry;

import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;

import java.util.List;

public interface AuthorizationPolicyRegistry {
    List<AuthorizationPolicy> resolve(String resourceType, String action);
}
