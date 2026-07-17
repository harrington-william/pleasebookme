package com.pleasebookme.server.auth.apikey.controller;

import com.pleasebookme.server.auth.apikey.dto.ApiKeyRequest;
import com.pleasebookme.server.auth.apikey.dto.ApiKeyResponse;
import com.pleasebookme.server.auth.apikey.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponse createApiKey(@Valid @RequestBody ApiKeyRequest request) {
        return ApiKeyResponse.from(apiKeyService.createApiKey(request));
    }

    @GetMapping("/{apiKeyId}")
    public ApiKeyResponse getApiKey(@PathVariable BigInteger apiKeyId) {
        return ApiKeyResponse.from(apiKeyService.getApiKeyById(apiKeyId));
    }

    @GetMapping
    public List<ApiKeyResponse> getApiKeys() {
        return apiKeyService.getAllApiKeys().stream()
            .map(ApiKeyResponse::from)
            .toList();
    }

    @PutMapping("/{apiKeyId}")
    public ApiKeyResponse updateApiKey(
        @PathVariable BigInteger apiKeyId,
        @Valid @RequestBody ApiKeyRequest request
    ) {
        return ApiKeyResponse.from(apiKeyService.updateApiKey(apiKeyId, request));
    }

    @DeleteMapping("/{apiKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApiKey(@PathVariable BigInteger apiKeyId) {
        apiKeyService.deleteApiKey(apiKeyId);
    }
}
