package com.pleasebookme.server.security.token.jwt.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
    @NotBlank
    String issuer,

    @NotBlank
    String audience,

    @NotBlank
    String algorithm,

    @NotBlank
    String secret,

    @NotNull
    Duration accessTokenLifeTime,

    @NotNull
    Duration refreshTokenLifeTime
) {}
