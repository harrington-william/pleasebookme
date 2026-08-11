package com.pleasebookme.server.security.config.property;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
    @NotEmpty
    List<String> allowedOrigins,

    @NotEmpty
    List<String> allowedMethods,

    @NotEmpty
    List<String> allowedHeaders,

    List<String> exposedHeaders,

    boolean allowCredentials,

    @NotNull
    Duration maxAge
) {}
