package com.pleasebookme.server.security.crypto.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "security.token-encryption")
public record TokenEncryptionProperties(
    @NotNull
    Short currentVersion,

    @NotEmpty
    Map<Short, String> keys
) {}
