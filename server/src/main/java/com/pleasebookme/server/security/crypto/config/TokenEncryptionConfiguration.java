package com.pleasebookme.server.security.crypto.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Register configuration
@Configuration
@EnableConfigurationProperties(TokenEncryptionProperties.class)
public class TokenEncryptionConfiguration {
}
