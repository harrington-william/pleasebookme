package com.pleasebookme.server.auth.apikey.service.impl;

import com.pleasebookme.server.auth.apikey.dto.ApiKeyRequest;
import com.pleasebookme.server.auth.apikey.entity.ApiKeyEntity;
import com.pleasebookme.server.auth.apikey.exception.ApiKeyNotFoundException;
import com.pleasebookme.server.auth.apikey.exception.DuplicateApiKeyException;
import com.pleasebookme.server.auth.apikey.repository.ApiKeyRepository;
import com.pleasebookme.server.auth.apikey.service.ApiKeyService;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    @Override
    public ApiKeyEntity createApiKey(ApiKeyRequest request) {
        if (apiKeyRepository.existsByPublicKey(request.publicKey())) {
            throw new DuplicateApiKeyException("Public key already exists: " + request.publicKey());
        }

        UserEntity ownerUser = userRepository.findById(request.ownerUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.ownerUserId()));

        TenantEntity tenant = TenantEntity.builder()
            .tenantId(request.tenantId())
            .build();

        ApiKeyEntity.ApiKeyEntityBuilder apiKey = ApiKeyEntity.builder()
            .tenant(tenant)
            .ownerUser(ownerUser)
            .name(request.name())
            .description(request.description())
            .publicKey(request.publicKey())
            .secretHash(request.secretHash())
            .lastUsedAt(request.lastUsedAt())
            .expiresAt(request.expiresAt())
            .revokedAt(request.revokedAt());

        if (request.status() != null) apiKey.status(request.status());

        return apiKeyRepository.save(apiKey.build());
    }

    @Override
    public ApiKeyEntity getApiKeyById(BigInteger apiKeyId) {
        return apiKeyRepository.findById(apiKeyId)
            .orElseThrow(() -> new ApiKeyNotFoundException(
                "API key not found: " + apiKeyId
            ));
    }

    @Override
    public List<ApiKeyEntity> getAllApiKeys() {
        return apiKeyRepository.findAll();
    }

    @Override
    public ApiKeyEntity updateApiKey(
        BigInteger apiKeyId,
        ApiKeyRequest request
    ) {
        ApiKeyEntity apiKey = getApiKeyById(apiKeyId);

        UserEntity ownerUser = userRepository.findById(request.ownerUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.ownerUserId()));

        TenantEntity tenant = TenantEntity.builder()
            .tenantId(request.tenantId())
            .build();

        apiKey.setTenant(tenant);
        apiKey.setOwnerUser(ownerUser);
        apiKey.setName(request.name());
        apiKey.setDescription(request.description());
        apiKey.setPublicKey(request.publicKey());
        apiKey.setSecretHash(request.secretHash());
        apiKey.setLastUsedAt(request.lastUsedAt());
        apiKey.setExpiresAt(request.expiresAt());
        apiKey.setRevokedAt(request.revokedAt());

        if (request.status() != null) apiKey.setStatus(request.status());

        return apiKeyRepository.save(apiKey);
    }

    @Override
    public void deleteApiKey(BigInteger apiKeyId) {
        apiKeyRepository.delete(getApiKeyById(apiKeyId));
    }
}
