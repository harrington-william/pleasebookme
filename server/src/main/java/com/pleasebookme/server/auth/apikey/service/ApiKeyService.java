package com.pleasebookme.server.auth.apikey.service;

import com.pleasebookme.server.auth.apikey.dto.ApiKeyRequest;
import com.pleasebookme.server.auth.apikey.entity.ApiKeyEntity;

import java.math.BigInteger;
import java.util.List;

public interface ApiKeyService {
    ApiKeyEntity createApiKey(ApiKeyRequest request);

    ApiKeyEntity getApiKeyById(BigInteger apiKeyId);

    List<ApiKeyEntity> getAllApiKeys();

    ApiKeyEntity updateApiKey(BigInteger apiKeyId, ApiKeyRequest request);

    void deleteApiKey(BigInteger apiKeyId);
}
